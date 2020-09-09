package com.transport.irctc.TicketBooking.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
public class PaymentDetails {
    @NotNull
    private String paymentOrderId;

    private String paymentId;

    private String paymentSignature;
}
