package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Models.PassengerDetails;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Optional;

public class TrainWaitlist {

    @Getter
    private LinkedHashSet<PassengerDetails> waitList;

    private int maxWaitList;

    public TrainWaitlist() {
        maxWaitList = 400;
        waitList = new LinkedHashSet<>(maxWaitList + 10, 1);
    }

    // forceAdd should be set only if passenger's group has atleast one waitlist number allocated to a member in that group
    public Optional<Integer> addToWaitlist(PassengerDetails passengerDetails, boolean forceAdd) {
        if (waitList.size() >= maxWaitList && !forceAdd)
            return Optional.empty();
        waitList.add(passengerDetails);
        return Optional.of(waitList.size());
    }

    // allocate seats so that people with least waiting 'must' have the priority
//    public void allocateSeats(List<Seat> seats) {
//
//    }
}
