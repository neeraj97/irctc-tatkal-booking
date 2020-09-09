package com.transport.irctc.TicketBooking.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@UserDefinedType("passenger_details")
public class PassengerDetails {
    @NotNull
    private String name;

    @NotNull
    private int age;

    private SeatType preference;
}
