package com.transport.irctc.TicketBooking.Controllers;

import com.transport.irctc.TicketBooking.Events.TatkalEvent;
import com.transport.irctc.TicketBooking.Models.BookingDetails;
import com.transport.irctc.TicketBooking.Models.Coach;
import com.transport.irctc.TicketBooking.Models.PaymentDetails;
import com.transport.irctc.TicketBooking.Repositories.BookingDetailsRepository;
import com.transport.irctc.TicketBooking.Services.PricingService;
import com.transport.irctc.TicketBooking.Services.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    // Mocked bean
    @Autowired
    private PricingService pricingService;

    // Mocked bean
    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private BookingDetailsRepository bookingDetailsRepository;

    @Autowired
    private KafkaTemplate<Integer, TatkalEvent> kafkaTemplate;

    @GetMapping("hello")
    public String hello() {
        return "hello";
    }

    @PostMapping("details")
    public ResponseEntity<String> addBookingDetails(@RequestBody @Valid BookingDetails bookingDetails) {
        // Get the price for the booking
        double price = pricingService.getPrice(bookingDetails);

        // create the payment for the booking
        String paymentOrderId = paymentGatewayService.createPaymentForAmount(price);
        bookingDetails.setPaymentOrderId(paymentOrderId);

        // Insert the details into the DB
        bookingDetails.setBookingId(UUID.randomUUID().toString());
        bookingDetailsRepository.save(bookingDetails);

        return ResponseEntity.ok(paymentOrderId);
    }

    @GetMapping
    public List<BookingDetails> getBookingDetails() {
        List<BookingDetails> list = bookingDetailsRepository.findAll();
        return list;
    }

    @PostMapping("paymentdetails")
    public ResponseEntity confirmPaymentAndBook(@RequestBody @Valid PaymentDetails paymentDetails) throws ExecutionException, InterruptedException {

        if (!paymentGatewayService.verifyPaymentDetails(paymentDetails)) {
            throw new RuntimeException("Incorrect payment details");
        }

        // get booking details for the paymentOrderId
        BookingDetails bookingDetails = bookingDetailsRepository
                .findById(paymentDetails.getPaymentOrderId())
                .orElseThrow(() -> new RuntimeException("Booking details Not Found"));

        TatkalEvent bookTicketEvent = TatkalEvent.builder()
                .trainNumber(bookingDetails.getTrainNumber())
                .bookingDetails(bookingDetails)
                .build()
                .asBookingEvent();

        // raise event to kafka for booking
        SendResult<Integer, TatkalEvent> result = kafkaTemplate.sendDefault(bookTicketEvent.getTrainNumber(), bookTicketEvent).get();

        return ResponseEntity.ok().build();
    }

    @PostMapping("addTrain/{trainNumber}")
    public ResponseEntity addTrain(@PathVariable Integer trainNumber, @RequestBody List<Coach> coachList) throws ExecutionException, InterruptedException {
        // build tatkalevent
        TatkalEvent event = TatkalEvent.builder()
                .trainNumber(trainNumber)
                .coachesAddition(coachList)
                .build().asCoachAdditionEvent();
        // raise event to kafka
        kafkaTemplate.sendDefault(trainNumber, event).get();
        return ResponseEntity.ok().build();
    }
}