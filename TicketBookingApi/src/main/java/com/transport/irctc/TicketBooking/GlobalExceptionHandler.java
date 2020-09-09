package com.transport.irctc.TicketBooking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity handleAnyException(Exception ex){
        return ResponseEntity.badRequest().build();
    }
}
