package com.transport.irctc.TicketBooking.Serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.irctc.TicketBooking.Events.TatkalEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;

public class TatkalEventSerializer implements Serializer<TatkalEvent> {

    private String encoding = "UTF8";
    private ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public byte[] serialize(String topic, TatkalEvent data) {
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
}
