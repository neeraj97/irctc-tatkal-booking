package com.transport.irctc.TicketBooking.Services;

import com.transport.irctc.TicketBooking.Models.PaymentDetails;

public interface PaymentGatewayService {
    // Calls PaymentGateway to create a PaymentOrderId for the amount
    String createPaymentForAmount(double amount);

    // verifies the payment details
    boolean verifyPaymentDetails(PaymentDetails paymentDetails);
}
