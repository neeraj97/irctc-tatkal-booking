package com.transport.irctc.TicketBooking.Events;

import com.transport.irctc.TicketBooking.Models.BookingDetails;
import com.transport.irctc.TicketBooking.Models.SeatAllocation;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketBookingStatusEvent {
    private BookingDetails bookingDetails;
    private List<SeatAllocation> seatAllocationList;
}
