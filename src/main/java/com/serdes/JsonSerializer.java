package com.serdes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
public class JsonSerializer<T> implements Serializer<T> {

    @Override
    public byte[] serialize(String topic, T data) {
        ObjectMapper mapper = new ObjectMapper();
        byte[] retVal = null;
        try{
            mapper.writeValueAsBytes(data);
            retVal = mapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return retVal;

    }
}
