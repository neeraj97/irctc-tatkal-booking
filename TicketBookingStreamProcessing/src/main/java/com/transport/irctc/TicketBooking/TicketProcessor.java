package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Events.TicketBookingStatusEvent;
import com.transport.irctc.TicketBooking.Events.TatkalEvent;
import com.transport.irctc.TicketBooking.Models.*;
import lombok.Getter;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketProcessor implements Processor<Integer, TatkalEvent> {

    private @Getter final static String trainSeatInventoryStoreName = "TrainSeatsInventory";

    private @Getter final static String trainWaitlistStoreName = "TrainWaitlist";

    private KeyValueStore<Integer, TrainSeatInventory> trainSeatInventoryKVStore;
    private KeyValueStore<Integer, TrainWaitlist> trainWaitlistKVStore;
    private ProcessorContext context;
    private int totalSeatTypes;
    private SeatType[] seatTypes;

    @Override
    public void init(ProcessorContext processorContext) {
        context = processorContext;
        trainSeatInventoryKVStore = (KeyValueStore) processorContext.getStateStore(trainSeatInventoryStoreName);
        trainWaitlistKVStore = (KeyValueStore) processorContext.getStateStore(trainWaitlistStoreName);
        seatTypes = SeatType.values();
        totalSeatTypes = seatTypes.length;
    }

    @Override
    public void process(Integer trainNumber, TatkalEvent tatkalEvent) {
        if (tatkalEvent.ifBookingEvent()) {
            BookingDetails bookingDetails = tatkalEvent.getBookingDetails();
            TicketBookingStatusEvent event = bookTickets(trainNumber, bookingDetails);
            context.forward(trainNumber, event);
        } else if (tatkalEvent.ifCoachAdditionEvent()) {
            TrainSeatInventory trainSeatInventory = trainSeatInventoryKVStore.get(trainNumber);
            if (trainSeatInventory == null)
                trainSeatInventory = new TrainSeatInventory();
            trainSeatInventory.addCoaches(tatkalEvent.getCoachesAddition());
            trainSeatInventoryKVStore.put(trainNumber, trainSeatInventory);
        }
    }

    // Business Logic for booking tickets
    private TicketBookingStatusEvent bookTickets(Integer trainNumber, BookingDetails bookingDetails) {
        TrainSeatInventory trainSeatInventory = trainSeatInventoryKVStore.get(trainNumber);
        if (trainSeatInventory == null)
            return getNOTBOOKEDStatusSeatAllocation(bookingDetails);
        TrainWaitlist trainWaitList = trainWaitlistKVStore.get(trainNumber);
        if (trainWaitList == null)
            trainWaitList = new TrainWaitlist();

        List<PassengerDetails> passengerDetailsList = bookingDetails.getPassengerDetails();
        // re-arrange passengerDetailsList --> no preference seat type passengers should be at the end
        passengerDetailsList = rearrangePassengerDetailsList(passengerDetailsList);
        bookingDetails.setPassengerDetails(passengerDetailsList);

        // if customer only wants confirmed seats, make sure not to book if seats are not available
        if (bookingDetails.isBookOnlyIfSeatsAreAvailable()) {
            if (passengerDetailsList.size() > trainSeatInventory.getTotalAvailableSeatsCount()) {
                return getNOTBOOKEDStatusSeatAllocation(bookingDetails);
            }

            // if the customer wants only preferred seat type,  make sure they are available else don't book
            if (bookingDetails.isBookOnlyPreferredSeatTypes()) {
                int[] preferredSeatTypesCount = new int[totalSeatTypes];
                for (PassengerDetails passengerDetails : passengerDetailsList) {
                    SeatType seatPreference = passengerDetails.getPreference();
                    if (seatPreference != null)
                        preferredSeatTypesCount[seatPreference.ordinal()]++;
                }
                for (int i = 0; i < totalSeatTypes; i++) {
                    if (preferredSeatTypesCount[i] != 0 && trainSeatInventory.getNumberOfSeatsAvailableForGivenSeatType(seatTypes[i]) < preferredSeatTypesCount[i])
                        return getNOTBOOKEDStatusSeatAllocation(bookingDetails);
                }
            }
        }

        boolean waitListForceAdd = false;
        List<SeatAllocation> seatAllocations = new ArrayList<>(passengerDetailsList.size());
        for (PassengerDetails passengerDetails : passengerDetailsList) {

            Optional<Seat> allocatedSeat;
            if (passengerDetails.getPreference() == null)
                allocatedSeat = trainSeatInventory.allocateAnySeat();
            else {
                allocatedSeat = trainSeatInventory.allocateSeatOfGivenSeatType(passengerDetails.getPreference());
                // If there is no seat of given type available and customer is okay with any other type, try to allocate any other type
                if (allocatedSeat.isEmpty() && !bookingDetails.isBookOnlyPreferredSeatTypes())
                    allocatedSeat = trainSeatInventory.allocateAnySeat();
            }

            if (allocatedSeat.isPresent()) {
                seatAllocations.add(SeatAllocation.builder()
                        .passengerDetails(passengerDetails)
                        .bookingStatus(BookingStatus.CONFIRMED)
                        .seat(allocatedSeat.get())
                        .build());
                waitListForceAdd = true;
            } else {
                Optional<Integer> waitListNumber = trainWaitList.addToWaitlist(passengerDetails, waitListForceAdd);
                if (waitListNumber.isPresent()) {
                    seatAllocations.add(SeatAllocation.builder()
                            .passengerDetails(passengerDetails)
                            .bookingStatus(BookingStatus.WAITLIST)
                            .waitListNumber(waitListNumber.get()).build());
                    waitListForceAdd = true;
                } else {
                    return getNOTBOOKEDStatusSeatAllocation(bookingDetails);
                }
            }
        }

        // update key value stores
        trainSeatInventoryKVStore.put(trainNumber, trainSeatInventory);
        trainWaitlistKVStore.put(trainNumber, trainWaitList);

        return TicketBookingStatusEvent.builder()
                .seatAllocationList(seatAllocations)
                .bookingDetails(bookingDetails)
                .build();
    }

    // rearranging passenger list to move passengers with no preference to the end so that the preferred seats allocated to them
    private List<PassengerDetails> rearrangePassengerDetailsList(List<PassengerDetails> passengerDetailsList) {
        ArrayDeque<PassengerDetails> arrayDeque = new ArrayDeque<>(passengerDetailsList.size());
        for (PassengerDetails passengerDetails : passengerDetailsList) {
            if (passengerDetails.getPreference() == null)
                arrayDeque.addLast(passengerDetails);
            else
                arrayDeque.addFirst(passengerDetails);
        }
        return new ArrayList<>(arrayDeque);
    }

    //
    private TicketBookingStatusEvent getNOTBOOKEDStatusSeatAllocation(BookingDetails bookingDetails) {
        List<PassengerDetails> passengerDetailsList = bookingDetails.getPassengerDetails();
        List<SeatAllocation> seatAllocations = new ArrayList<>(passengerDetailsList.size());
        passengerDetailsList.forEach(passengerDetails ->
                seatAllocations.add(SeatAllocation.builder()
                        .bookingStatus(BookingStatus.NOT_BOOKED)
                        .passengerDetails(passengerDetails)
                        .build()));
        return TicketBookingStatusEvent.builder()
                .bookingDetails(bookingDetails)
                .seatAllocationList(seatAllocations)
                .build();
    }

    @Override
    public void close() {
        // close the opened resources
        // dont close the state stores , they are managed by streams library
    }
}