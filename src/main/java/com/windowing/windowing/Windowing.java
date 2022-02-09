package com.windowing.windowing;

import com.model.Almacen;
import com.model.ClientPlu;
import com.model.Cliente;
import com.model.Plu;
import com.serdes.SerdeFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.*;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Windowing {


    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "windowing");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 1048576);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        Serde<Plu> pluSerde= new SerdeFactory<Plu>().getSerde(Plu.class);
        pluSerde.serializer();

        Serde<Cliente> clienteSerde= new SerdeFactory<Cliente>().getSerde(Cliente.class);
        clienteSerde.serializer();

        Serde<ClientPlu> clientPluSerde= new SerdeFactory<ClientPlu>().getSerde(ClientPlu.class);
        clientPluSerde.serializer();

        KStream<String, Plu> pluKStream = builder
                .stream("plu",
                Consumed.with(Serdes.String(), pluSerde));

        GlobalKTable<String, Cliente> clienteGlobalKTable = builder
                .globalTable( "cliente",
                Materialized.<String, Cliente,
                                KeyValueStore<Bytes, byte[]>>as("store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(new SerdeFactory<Cliente>().getSerde(Cliente.class)));

        KStream<String, ClientPlu> joined = pluKStream
                .join(clienteGlobalKTable,
                (key, value) ->value.getIdTrasaccion(),
                (value1,value2)->{return new ClientPlu(value2.getIdTrasaccion(), value2.getCliente(),value1.getPlu());}
                );

        joined.to("clientePlu", Produced.with(Serdes.String(),clientPluSerde));

        KStream<String, ClientPlu> clientePlu = builder
                .stream("clientePlu",
                        Consumed.with(Serdes.String(), clientPluSerde))
                .selectKey((key, value) -> value.getCliente());

        // **************************************Reduce()
        KTable <Windowed<String>, ClientPlu> windowing = clientePlu
                .groupByKey(Grouped.with(Serdes.String(),clientPluSerde))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(60)).grace(Duration.ofSeconds(1)))
                .reduce((aggValue, newValue)->{
                    newValue.setPlu(newValue.getPlu() + "," + aggValue.getPlu());
                    return newValue;
                },Materialized.<String, ClientPlu, WindowStore<Bytes, byte[]>>as("latest-statistic-store")
                        .withValueSerde(clientPluSerde)
                        .withRetention(Duration.ofSeconds(70)))   //,Materialized.as("Prueba")
                .suppress(Suppressed.untilWindowCloses(unbounded()));

        windowing.toStream().to("prueba1");

        //.foreach((key, value) -> System.out.println(key + " : " +  key.window().hashCode() + " : "+ value ));

        // **************************************COUNT()
        /*KTable <Windowed<String>, Long> windowingCount = clientePlu
                .groupByKey(Grouped.with(Serdes.String(),clientPluSerde))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(60)).grace(Duration.ofSeconds(1)))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>
                        as("Prueba2").withRetention(Duration.ofHours(6)))   //,Materialized.as("Prueba")
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()));

        windowingCount.toStream().foreach((key, value) -> System.out.println(key + " : " +  key.window().hashCode() + " : "+ value ));*/


        //*************************************** PRUEBA CON GROUPBY
        /*KTable <Windowed<String>, Long> windowing = joined.groupBy((key, value) -> value.getCliente())
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
                .count();

        windowing.toStream().foreach((key, value) -> System.out.println(key + " : " +  key.window().hashCode() + "Count: "+ value ));*/


        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            streams.cleanUp();
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);

    }
}
