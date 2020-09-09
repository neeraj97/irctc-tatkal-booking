package com.transport.irctc.TicketBooking.Services;

import com.transport.irctc.TicketBooking.Models.BookingDetails;

public interface PricingService {
    // This method validates the booking details and calculates the total price for the Booking
    double getPrice(BookingDetails bookingDetails);
}
