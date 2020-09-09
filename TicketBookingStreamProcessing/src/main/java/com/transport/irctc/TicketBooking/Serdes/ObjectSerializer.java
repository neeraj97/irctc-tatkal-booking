package com.transport.irctc.TicketBooking.Serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class ObjectSerializer<T> implements Serializer<T> {

    private String encoding = "UTF8";
    private ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // do nothing
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            if (data == null)
                return null;
            String dataStr = objectMapper.writeValueAsString(data);
            return dataStr.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException("Error when serializing TatkalEvent to byte[] due to unsupported encoding " + encoding);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error when serializing TatkalEvent to byte[] due to JsonProcessing");
        }
    }

    @Override
    public void close() {
        // do nothing
    }
}
