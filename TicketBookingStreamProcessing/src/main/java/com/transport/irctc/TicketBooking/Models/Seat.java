package com.transport.irctc.TicketBooking.Models;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Seat {
    private int coachNumber;
    private int seatNumber;
    private SeatType type;
}
