package com.transport.irctc.TicketBooking.Events;

import com.transport.irctc.TicketBooking.Models.BookingDetails;
import com.transport.irctc.TicketBooking.Models.Coach;
import com.transport.irctc.TicketBooking.Models.Seat;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TatkalEvent {

    private Integer trainNumber;

    private EventType eventType;

    private List<Coach> coachesAddition;

    private BookingDetails bookingDetails;

    private List<Seat> cancellationSeats;

    public TatkalEvent asBookingEvent() {
        this.eventType = EventType.BOOKING;
        return this;
    }

    public TatkalEvent asCoachAdditionEvent() {
        this.eventType = EventType.COACH_ADDITION;
        return this;
    }

    public boolean ifBookingEvent() {
        return this.eventType.compareTo(EventType.BOOKING) == 0;
    }

    public boolean ifCoachAdditionEvent() {
        return this.eventType.compareTo(EventType.COACH_ADDITION) == 0;
    }
}