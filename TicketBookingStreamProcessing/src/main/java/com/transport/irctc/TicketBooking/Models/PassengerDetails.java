package com.transport.irctc.TicketBooking.Models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDetails {
    private String name;

    private int age;

    private SeatType preference;
}
