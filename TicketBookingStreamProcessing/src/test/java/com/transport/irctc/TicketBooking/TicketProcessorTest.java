package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Events.EventType;
import com.transport.irctc.TicketBooking.Events.TatkalEvent;
import com.transport.irctc.TicketBooking.Events.TicketBookingStatusEvent;
import com.transport.irctc.TicketBooking.Models.*;
import com.transport.irctc.TicketBooking.Serdes.TrainSeatInventorySerde;
import com.transport.irctc.TicketBooking.Serdes.TrainWaitlistSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class TicketProcessorTest {

    final Processor processorUnderTest = new TicketProcessor();
    final MockProcessorContext context = new MockProcessorContext();
    KeyValueStore<Integer, TrainSeatInventory> trainSeatInventoryKVStore;
    KeyValueStore<Integer, TrainWaitlist> trainWaitlistKVStore;
    Serde<TrainSeatInventory> trainSeatInventorySerde = new TrainSeatInventorySerde();
    Serde<TrainWaitlist> trainWaitlistSerde = new TrainWaitlistSerde();

    @Before
    public void setup() {
        // train seat inventory store
        trainSeatInventoryKVStore =
                Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(TicketProcessor.getTrainSeatInventoryStoreName()),
                        Serdes.Integer(),
                        trainSeatInventorySerde
                )
                        .withLoggingDisabled() // Changelog is not supported by MockProcessorContext.
                        .build();
        trainSeatInventoryKVStore.init(context, trainSeatInventoryKVStore);
        context.register(trainSeatInventoryKVStore, /*parameter unused in mock*/ null);

        // train waitlist store
        trainWaitlistKVStore =
                Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(TicketProcessor.getTrainWaitlistStoreName()),
                        Serdes.Integer(),
                        trainWaitlistSerde
                )
                        .withLoggingDisabled() // Changelog is not supported by MockProcessorContext.
                        .build();
        trainWaitlistKVStore.init(context, trainWaitlistKVStore);
        context.register(trainWaitlistKVStore, /*parameter unused in mock*/ null);
        processorUnderTest.init(context);
    }

    @Test
    public void givenCoachAdditionTatkalEvent_WhenProcess_ThenAddTheCoachToTrainSeatInventoryKVStore() {
        // Arrange
        int coachNumber = 1;
        int numOfSeats = 60;
        int trainNumber = 123;
        List<Coach> coachList = Collections.singletonList(generateCoach(coachNumber, numOfSeats));
        TatkalEvent event = TatkalEvent.builder()
                .eventType(EventType.COACH_ADDITION)
                .coachesAddition(coachList)
                .trainNumber(trainNumber)
                .build();

        // Act
        processorUnderTest.process(trainNumber, event);

        // Assert
        Assertions.assertThat(trainSeatInventoryKVStore.get(trainNumber).getTotalAvailableSeatsCount()).isEqualTo(numOfSeats);
    }

    @Test
    public void givenSeatsAvailable_WhenProcessBookingTatkalEvent_ThenForwardTicketBookingStatusEventWithSeatsAllocated() {
        // Arrange
        int trainNumber = 123;
        int numOfSeats = 60;
        createAndProcessCoachAdditionEvent(1, numOfSeats, trainNumber);
        List<PassengerDetails> passengerDetailsList = generatePassengerDetailsList();
        BookingDetails bookingDetails = BookingDetails.builder().passengerDetails(passengerDetailsList).build();

        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber, tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getSeat()).isNotNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.CONFIRMED);
        }
        Assertions.assertThat(trainSeatInventoryKVStore.get(trainNumber).getTotalAvailableSeatsCount()).isEqualTo(numOfSeats - passengerDetailsList.size());
    }

    @Test
    public void givenTrainNotAvailable_WhenProcessBookingTatkalEvent_ThenForwardTicketBookingStatusEventWithNoSeatsAllocated() {
        // Arrange
        int trainNumber = 123;
        List<PassengerDetails> passengerDetailsList = generatePassengerDetailsList();
        BookingDetails bookingDetails = BookingDetails.builder()
                .passengerDetails(passengerDetailsList)
                .build();
        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber, tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getSeat()).isNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.NOT_BOOKED);
        }
    }

    @Test
    public void givenSeatsNotAvailable_WhenProcessBookingTatkalEvent_ThenForwardTicketBookingStatusEventWithWaitlistNumber() {
        // Arrange
        int trainNumber = 123;
        int numOfSeats = 0;
        createAndProcessCoachAdditionEvent(1, numOfSeats, trainNumber);
        List<PassengerDetails> passengerDetailsList = generatePassengerDetailsList();
        BookingDetails bookingDetails = BookingDetails.builder()
                .passengerDetails(passengerDetailsList)
                .build();
        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber, tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getWaitListNumber()).isNotNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.WAITLIST);
        }
    }

    @Test
    public void givenSeatsNotAvailable_WhenProcessBookingTatkalEventWithBookOnlyIfSeatsAreAvailable_ThenForwardTicketBookingStatusEventWithNoSeatsAllocated() {
        // Arrange
        int trainNumber = 123;
        int numOfSeats = 0;
        createAndProcessCoachAdditionEvent(1, numOfSeats, trainNumber);
        List<PassengerDetails> passengerDetailsList = generatePassengerDetailsList();
        BookingDetails bookingDetails = BookingDetails.builder()
                .passengerDetails(passengerDetailsList)
                .bookOnlyIfSeatsAreAvailable(true)
                .build();
        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber, tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getWaitListNumber()).isNull();
            Assertions.assertThat(seatAllocation.getSeat()).isNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.NOT_BOOKED);
        }
    }

    @Test
    public void givenPreferredSeatsNotAvailable_WhenProcessBookingTatkalEvent_ThenForwardTicketBookingStatusEventWithSeatAllocatedOfOtherType() {
        // Arrange
        // Add some seat
        int trainNumber = 123;
        TrainSeatInventory trainSeatInventory = new TrainSeatInventory();
        Seat seat = Seat.builder().seatNumber(1).coachNumber(1).type(SeatType.UPPER).build();
        trainSeatInventory.addSeats(Collections.singletonList(seat));
        trainSeatInventoryKVStore.put(trainNumber, trainSeatInventory);
        // Prepare booking details
        PassengerDetails passengerDetails = PassengerDetails.builder().name("John").age(31).preference(SeatType.LOWER).build();
        BookingDetails bookingDetails = BookingDetails.builder()
                .trainNumber(trainNumber)
                .passengerDetails(Collections.singletonList(passengerDetails))
                .build();
        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber, tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getSeat()).isNotNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.CONFIRMED);
        }
    }

    @Test
    public void givenPreferredSeatsNotAvailable_WhenProcessBookingTatkalEventWithBookOnlyPreferredSeatTypes_ThenForwardTicketBookingStatusEventWithWaitlistNumber() {
        // Arrange
        // Add some seat
        int trainNumber = 123;
        TrainSeatInventory trainSeatInventory = new TrainSeatInventory();
        Seat seat = Seat.builder().seatNumber(1).coachNumber(1).type(SeatType.UPPER).build();
        trainSeatInventory.addSeats(Collections.singletonList(seat));
        trainSeatInventoryKVStore.put(trainNumber, trainSeatInventory);
        // Prepare booking details
        PassengerDetails passengerDetails = PassengerDetails.builder().name("John").age(31).preference(SeatType.LOWER).build();
        BookingDetails bookingDetails = BookingDetails.builder()
                .trainNumber(trainNumber)
                .passengerDetails(Collections.singletonList(passengerDetails))
                .bookOnlyPreferredSeatTypes(true)
                .build();
        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber, tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getWaitListNumber()).isNotNull();
            Assertions.assertThat(seatAllocation.getSeat()).isNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.WAITLIST);
        }
    }

    @Test
    public void givenPreferredSeatsNotAvailable_WhenProcessBookingTatkalEventWithBookOnlyPreferredSeatTypesAndBookOnlyIfSeatsAreAvailable_ThenForwardTicketBookingStatusEventWithNoSeatsAllocated() {
        // Arrange
        // Add some seat
        int trainNumber = 123;
        TrainSeatInventory trainSeatInventory = new TrainSeatInventory();
        Seat seat = Seat.builder().seatNumber(1).coachNumber(1).type(SeatType.UPPER).build();
        trainSeatInventory.addSeats(Collections.singletonList(seat));
        trainSeatInventoryKVStore.put(trainNumber, trainSeatInventory);
        // Prepare booking details
        PassengerDetails passengerDetails = PassengerDetails.builder().name("John").age(31).preference(SeatType.LOWER).build();
        BookingDetails bookingDetails = BookingDetails.builder()
                .trainNumber(trainNumber)
                .passengerDetails(Collections.singletonList(passengerDetails))
                .bookOnlyPreferredSeatTypes(true)
                .bookOnlyIfSeatsAreAvailable(true)
                .build();
        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber, tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getWaitListNumber()).isNull();
            Assertions.assertThat(seatAllocation.getSeat()).isNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.NOT_BOOKED);
        }
    }

    // This is the test for how good does the algorithm allocate seats
    @Test
    public void givenExactNumberOfSeatsPreferredSeatsAvailable_WhenProcessBookingTatkalEvent_ThenForwardTicketBookingStatusEventWithSeatsAllocated(){
        // Arrange
        // add three seats with LOWER,LOWER,UPPER and passengers with preference NONE,LOWER,LOWER
        int trainNumber = 123;
        TrainSeatInventory trainSeatInventory = new TrainSeatInventory();
        Seat seat1 = Seat.builder().seatNumber(1).coachNumber(1).type(SeatType.LOWER).build();
        Seat seat2 = Seat.builder().seatNumber(1).coachNumber(2).type(SeatType.LOWER).build();
        Seat seat3 = Seat.builder().seatNumber(1).coachNumber(3).type(SeatType.UPPER).build();
        trainSeatInventory.addSeats(Arrays.asList(seat1,seat2,seat3));
        trainSeatInventoryKVStore.put(trainNumber, trainSeatInventory);

        PassengerDetails passengerDetails1 = PassengerDetails.builder().name("John").age(31).build();
        PassengerDetails passengerDetails2 = PassengerDetails.builder().name("Jay").age(31).preference(SeatType.LOWER).build();
        PassengerDetails passengerDetails3 = PassengerDetails.builder().name("Jane").age(31).preference(SeatType.LOWER).build();
        BookingDetails bookingDetails = BookingDetails.builder()
                .trainNumber(trainNumber)
                .passengerDetails(Arrays.asList(passengerDetails1,passengerDetails2,passengerDetails3))
                .bookOnlyPreferredSeatTypes(true)
                .bookOnlyIfSeatsAreAvailable(true)
                .build();
        TatkalEvent tatkalEvent = TatkalEvent.builder()
                .bookingDetails(bookingDetails)
                .trainNumber(trainNumber).build()
                .asBookingEvent();

        // Act
        processorUnderTest.process(trainNumber,tatkalEvent);

        // Assert
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();
        Assertions.assertThat(forwarded.hasNext()).isTrue();
        TicketBookingStatusEvent event = (TicketBookingStatusEvent) forwarded.next().keyValue().value;
        for (SeatAllocation seatAllocation : event.getSeatAllocationList()) {
            Assertions.assertThat(seatAllocation.getSeat()).isNotNull();
            Assertions.assertThat(seatAllocation.getBookingStatus()).isEqualByComparingTo(BookingStatus.CONFIRMED);
        }
    }

    private List<PassengerDetails> generatePassengerDetailsList(int ...args) {
        Random random = new Random();
        SeatType preferences[] = SeatType.values();
        String[] names = {"Joe", "Ronald", "Chris", "Chuck", "Lovren", "Drake", "Nick", "Stera", "Kaira", "Abhi", "James", "George", "Williams",
                "Mohan D'Souza", "Sergio", "Thiago", "David", "Micheal", "Ramaz", "Romavic", "Mavich"};

        // create Passenger List
        List<PassengerDetails> passengerlist = new ArrayList<>();
        int numOfPassengers = args.length == 0 ? random.nextInt(6) + 1 : args[0];
        for (int i = 0; i < numOfPassengers; i++) {
            PassengerDetails passengerDetails = PassengerDetails.builder()
                    .age(random.nextInt(100) + 1)
                    .preference(preferences[random.nextInt(preferences.length)])
                    .name(names[random.nextInt(names.length)])
                    .build();
            passengerlist.add(passengerDetails);
        }
        return passengerlist;
    }

    private void createAndProcessCoachAdditionEvent(int coachNumber, int numOfSeats, int trainNumber) {

        List<Coach> coachList = Collections.singletonList(generateCoach(coachNumber, numOfSeats));
        TatkalEvent event = TatkalEvent.builder()
                .eventType(EventType.COACH_ADDITION)
                .coachesAddition(coachList)
                .trainNumber(trainNumber)
                .build();

        processorUnderTest.process(trainNumber, event);
    }

    private Coach generateCoach(int coachNumber, int numOfSeats) {
        SeatType[] types = SeatType.values();
        List<Seat> seats = new ArrayList<>(numOfSeats);
        for (int i = 0; i < numOfSeats; i++) {
            seats.add(Seat.builder().coachNumber(coachNumber).seatNumber(i + 1).type(types[i % types.length]).build());
        }
        Coach coach = Coach.builder().coachNumber(coachNumber).seats(seats).build();
        return coach;
    }
}