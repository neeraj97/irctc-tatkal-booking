package com.transport.irctc.TicketBooking.Models;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetails {

    public String paymentOrderId;

    private String bookingId;

    private Integer trainNumber;

    private String boardStationCode;

    private String destinationStationCode;

    private List<PassengerDetails> passengerDetails;

    private boolean bookOnlyPreferredSeatTypes;

    private boolean bookOnlyIfSeatsAreAvailable;

}
