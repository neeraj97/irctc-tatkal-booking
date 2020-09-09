package com.transport.irctc.TicketBooking.Controllers;

import com.transport.irctc.TicketBooking.Models.BookingDetails;
import com.transport.irctc.TicketBooking.Services.PricingService;
import com.transport.irctc.TicketBooking.Services.PaymentGatewayService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnit4.class)
public class BookingControllerTest {


    @Mock
    private PricingService pricingService;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private BookingController bookingController;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenBookingDetailsPriceWorthZero_WhenAddBookingDetails_ThenReturnBadRequest(){
        // Arrange
        BookingDetails bookingDetails = BookingDetails.builder().build();
        when(pricingService.getPrice(bookingDetails)).thenReturn(0.0);

        // Act
        ResponseEntity responseEntity = bookingController.addBookingDetails(bookingDetails);

        // Assert
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(BAD_REQUEST.value());
        verify(pricingService).getPrice(bookingDetails);
    }

}
