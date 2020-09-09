package com.transport.irctc.TicketBooking.Models;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Coach {
    private int coachNumber;
    private List<Seat> seats;
}
