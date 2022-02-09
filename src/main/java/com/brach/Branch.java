package com.brach;

import com.model.ClientPlu;
import com.model.Cliente;
import com.model.Plu;
import com.serdes.SerdeFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Branch {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "branch");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        Serde<Plu> pluSerde = new SerdeFactory<Plu>().getSerde(Plu.class);
        pluSerde.serializer();

        Serde<Cliente> clienteSerde = new SerdeFactory<Cliente>().getSerde(Cliente.class);
        clienteSerde.serializer();

        Serde<ClientPlu> clientPluSerde = new SerdeFactory<ClientPlu>().getSerde(ClientPlu.class);
        clientPluSerde.serializer();

        KStream<String, Plu> pluKStream = builder.stream("plu",
                Consumed.with(Serdes.String(), pluSerde));

        KStream<String, Plu>[] branches= pluKStream.branch(
                (key, value) -> key.startsWith("A"), /* first predicate  */
                (key, value) -> key.startsWith("B"), /* second predicate */
                (key, value) -> true                 /* third predicate  */
        );
        pluKStream.foreach((key, value) -> System.out.println(key + ": "+ value.toString()));
        branches[0].foreach((key, value) -> System.out.println("A: " + value.toString()));
        branches[1].foreach((key, value) -> System.out.println("B: " + value.toString()));
        branches[2].foreach((key, value) -> System.out.println("C: " + value.toString()));

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        streams.start();


    }
}