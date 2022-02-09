package com.join;

import com.model.*;
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

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.*;

public class Join {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "join");
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

        Serde<Almacen> almacenSerde= new SerdeFactory<Almacen>().getSerde(Almacen.class);
        almacenSerde.serializer();

        Serde<Caja> cajaSerde= new SerdeFactory<Caja>().getSerde(Caja.class);
        cajaSerde.serializer();

        Serde<ClientPlu> clientPluSerde= new SerdeFactory<ClientPlu>().getSerde(ClientPlu.class);
        clientPluSerde.serializer();

        Serde<ClientPluAlma> clientPluAlmaSerde= new SerdeFactory<ClientPluAlma>().getSerde(ClientPluAlma.class);
        clientPluAlmaSerde.serializer();

        Serde<ClientPluAlmaCaja> clientPluAlmaCajaSerde= new SerdeFactory<ClientPluAlmaCaja>().getSerde(ClientPluAlmaCaja.class);
        clientPluAlmaCajaSerde.serializer();


        KStream<String, Plu> pluKStream = builder
                .stream("plu",
                        Consumed.with(Serdes.String(), pluSerde));

        //pluKStream.to("prueba", Produced.with(Serdes.String(),pluSerde));

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


        GlobalKTable<String, Almacen> almacenGlobalKTable = builder
                .globalTable( "almacen",
                        Materialized.<String, Almacen,
                                        KeyValueStore<Bytes, byte[]>>as("store1")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new SerdeFactory<Almacen>().getSerde(Almacen.class)));

        KStream<String, ClientPluAlma> joinedAlmacen = joined
                .join(almacenGlobalKTable,
                        (key, value) ->value.getIdTrasaccion(),
                        (value1,value2)->{return new ClientPluAlma(value2.getIdTrasaccion(), value1.getCliente(),value1.getPlu(), value2.getAlmacen());}
                );

        joinedAlmacen.to("clientePluAlma", Produced.with(Serdes.String(),clientPluAlmaSerde));

        GlobalKTable<String, Caja> cajaGlobalKTable = builder
                .globalTable( "caja",
                        Materialized.<String, Caja,
                                        KeyValueStore<Bytes, byte[]>>as("store2")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new SerdeFactory<Caja>().getSerde(Caja.class)));

        KStream<String, ClientPluAlmaCaja> joinedCaja = joinedAlmacen
                .join(cajaGlobalKTable,
                        (key, value) ->value.getIdTrasaccion(),
                        (value1,value2)->{return new ClientPluAlmaCaja(value2.getIdTrasaccion(), value1.getCliente(),value1.getPlu(), value1.getAlmacen(),value2.getCaja());}
                );

        joinedCaja.to("clientePluAlmaCaja", Produced.with(Serdes.String(),clientPluAlmaCajaSerde));


       KStream<String, ClientPluAlmaCaja> clientePluAlmaCaja = builder
                .stream("clientePluAlmaCaja",
                        Consumed.with(Serdes.String(), clientPluAlmaCajaSerde))
                .selectKey((key, value) -> value.getCliente());

       KTable <Windowed<String>, ClientPluAlmaCaja> windowing = clientePluAlmaCaja
                .groupByKey(Grouped.with(Serdes.String(),clientPluAlmaCajaSerde))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(60)).grace(Duration.ofSeconds(1)))
                .reduce((aggValue, newValue)->{
                    newValue.setPlu(newValue.getPlu() + "," + aggValue.getPlu());
                    return newValue;
                },Materialized.<String, ClientPluAlmaCaja, WindowStore<Bytes, byte[]>>as("latest-joined-store")
                        .withValueSerde(clientPluAlmaCajaSerde)
                        .withRetention(Duration.ofSeconds(70)))   //,Materialized.as("Prueba")
                .suppress(Suppressed.untilWindowCloses(unbounded()));

       windowing.toStream().to("prueba3");

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
