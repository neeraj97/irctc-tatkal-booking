package com.transport.irctc.TicketBooking.Models;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatAllocation {
    private Seat seat;
    private Integer waitListNumber;
    private PassengerDetails passengerDetails;
    private BookingStatus bookingStatus;
}
