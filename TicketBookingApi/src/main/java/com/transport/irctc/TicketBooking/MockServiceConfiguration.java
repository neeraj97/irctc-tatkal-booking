package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Models.BookingDetails;
import com.transport.irctc.TicketBooking.Models.PaymentDetails;
import com.transport.irctc.TicketBooking.Services.PricingService;
import com.transport.irctc.TicketBooking.Services.PaymentGatewayService;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.UUID;

@Configuration
public class MockServiceConfiguration {

    private Random random=new Random();

    // Mocking BookingDetailsService to return some random price
    @Bean
    public PricingService pricingService() {

        PricingService pricingService = Mockito.mock(PricingService.class);

        Mockito.when(pricingService.getPrice(ArgumentMatchers.any(BookingDetails.class))).then(invocation -> {
            BookingDetails bookingDetails = invocation.getArgument(0,BookingDetails.class);
            return random.nextInt(50)*100*1.0 + 1;
        });

        return pricingService;
    }

    // Mocking PaymentGatewayService to create and return random PaymentOrderId
    @Bean
    public PaymentGatewayService paymentGatewayService(){
        PaymentGatewayService paymentGatewayService = Mockito.mock(PaymentGatewayService.class);

        Mockito.when(paymentGatewayService.createPaymentForAmount(ArgumentMatchers.anyDouble())).then(invocation -> {
            // delaying
            Thread.sleep(2000);
            return UUID.randomUUID().toString();
        });

        Mockito.when(paymentGatewayService.verifyPaymentDetails(ArgumentMatchers.any(PaymentDetails.class))).then(invocation -> {
            // delaying
            Thread.sleep(2000);
//            return random.nextInt(100)!=0;
            return true;
        });

        return paymentGatewayService;
    }


}
