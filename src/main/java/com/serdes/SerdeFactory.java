package com.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public  final class SerdeFactory<T> {

    public Serde<T> getSerde(Class<T> t) {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("JsonPOJOClass", t);

        final Serializer<T> serializer = new JsonSerializer<>();
        serializer.configure(serdeProps, false);

        final Deserializer<T> deserializer = new JsonDeserializer<>();
        deserializer.configure(serdeProps, false);

        final Serde<T> itemPriceSerde = Serdes.serdeFrom(serializer, deserializer);

        return itemPriceSerde;
    }
}
