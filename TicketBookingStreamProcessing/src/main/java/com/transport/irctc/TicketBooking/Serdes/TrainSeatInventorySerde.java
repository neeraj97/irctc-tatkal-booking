package com.transport.irctc.TicketBooking.Serdes;

import com.transport.irctc.TicketBooking.TrainSeatInventory;
import org.apache.kafka.common.serialization.Serdes;

public final class TrainSeatInventorySerde extends Serdes.WrapperSerde<TrainSeatInventory> {
    public TrainSeatInventorySerde() {
        super(new ObjectSerializer<TrainSeatInventory>(), new ObjectDeserializer<TrainSeatInventory>(TrainSeatInventory.class));
    }
}
