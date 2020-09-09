package com.transport.irctc.TicketBooking.Serdes;

import com.transport.irctc.TicketBooking.TrainWaitlist;
import org.apache.kafka.common.serialization.Serdes;

public final class TrainWaitlistSerde extends Serdes.WrapperSerde<TrainWaitlist> {
    public TrainWaitlistSerde() {
        super(new ObjectSerializer<TrainWaitlist>(), new ObjectDeserializer<TrainWaitlist>(TrainWaitlist.class));
    }
}
