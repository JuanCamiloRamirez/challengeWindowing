package com.serdes;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.LinkedHashSet;
import java.util.Map;

public class JsonDeserializer<T> implements Deserializer<T> {

    private ObjectMapper objectMapper = new ObjectMapper();


    private Class<T> tClass;
    public void JsonPOJODeserializer(){
    }

    public void configure(Map<String, ?> props, boolean isKey) {
        tClass=(Class<T>) props.get("JsonPOJOClass");
    }


    public T deserialize(String topic, byte[] bytes) {
        if (bytes == null)
            return null;

        T data;

        try {
            if(tClass.equals(LinkedHashSet.class)) {
                TypeReference<LinkedHashSet<T>> typeReference=new TypeReference<>() {
                };
                LinkedHashSet<T> var=objectMapper.readValue(bytes, typeReference);
                data = (T)var;
            }else {
                data = objectMapper.readValue(bytes, tClass);
            }

        } catch (Exception e) {
            throw new SerializationException(e);
        }
        return data;
    }
}

