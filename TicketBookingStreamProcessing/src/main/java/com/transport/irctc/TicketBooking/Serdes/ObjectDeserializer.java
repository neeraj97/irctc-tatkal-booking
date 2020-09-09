package com.transport.irctc.TicketBooking.Serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class ObjectDeserializer<T> implements Deserializer<T> {
    private String encoding = "UTF8";
    private ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> classType;
    public ObjectDeserializer(Class<T> type){
        this.classType=type;
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public T  deserialize(String s, byte[] bytes) {
        if (bytes == null)
            return null;
        try {
            String json = new String(bytes, encoding);
            return objectMapper.readValue(json,classType);
        } catch (IOException e) {
            throw new SerializationException("Error occurred while deserializing "+e);
        }
    }

    @Override
    public void close() {

    }
}
