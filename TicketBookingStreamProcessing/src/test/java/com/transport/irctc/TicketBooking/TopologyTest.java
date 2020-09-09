package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Events.TatkalEvent;
import com.transport.irctc.TicketBooking.Events.TicketBookingStatusEvent;
import com.transport.irctc.TicketBooking.Models.*;
import com.transport.irctc.TicketBooking.Serdes.ObjectDeserializer;
import com.transport.irctc.TicketBooking.Serdes.ObjectSerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class TopologyTest {
    private TopologyTestDriver testDriver;
    private TestInputTopic<Integer,TatkalEvent> inputTopic;
    private TestOutputTopic<Integer, TicketBookingStatusEvent> outputTopic;

    @Before
    public void setup() {
        Topology topology = TicketBooker.buildTopology();
        Properties prop = TicketBooker.getStreamApplicationProperties();
        testDriver = new TopologyTestDriver(topology, prop);
        inputTopic = testDriver.createInputTopic(TicketBooker.sourceTopic, new IntegerSerializer()
                , new ObjectSerializer<TatkalEvent>());
        outputTopic = testDriver.createOutputTopic(TicketBooker.sinkTopic, new IntegerDeserializer()
                , new ObjectDeserializer<TicketBookingStatusEvent>(TicketBookingStatusEvent.class));
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void shouldUpdateTrainInventoryKVStoreForTrainCoachAdditionEvent() {
        // Arrange
        int trainNumber = 1234;
        int coachNumber = 1;
        int numOfSeats = 20;
        Coach coach = generateCoach(coachNumber, numOfSeats);
        TatkalEvent event = TatkalEvent.builder()
                .trainNumber(trainNumber)
                .coachesAddition(Collections.singletonList(coach))
                .build()
                .asCoachAdditionEvent();

        // Act
        inputTopic.pipeInput(trainNumber, event);

        // Assert
        KeyValueStore<Integer, TrainSeatInventory> store = testDriver.getKeyValueStore(TicketProcessor.getTrainSeatInventoryStoreName());
        TrainSeatInventory seatInventory = store.get(trainNumber);
        Assertions.assertThat(seatInventory).isNotNull();
        Assertions.assertThat(seatInventory.getTotalAvailableSeatsCount()).isEqualTo(numOfSeats);
    }

    @Test
    public void shouldOutputTicketBookingStatusEventForTicketBookingEvent() {
        // Arrange
        KeyValueStore<Integer, TrainSeatInventory> store = testDriver.getKeyValueStore(TicketProcessor.getTrainSeatInventoryStoreName());
        TrainSeatInventory seatInventory = new TrainSeatInventory();
        int coachNumber = 1;
        int numOfSeats = 60;
        int trainNumber = 123;
        seatInventory.addCoach(generateCoach(coachNumber, numOfSeats));
        store.put(trainNumber, seatInventory);
        BookingDetails bookingDetails = BookingDetails.builder()
                .passengerDetails(generatePassengerDetailsList())
                .build();
        TatkalEvent event = TatkalEvent.builder()
                .trainNumber(trainNumber)
                .bookingDetails(bookingDetails)
                .build()
                .asBookingEvent();

        // Act
        inputTopic.pipeInput(trainNumber, event);

        // Assert
        Assertions.assertThat(outputTopic.readValue()).isNotNull();
        Assertions.assertThat(outputTopic.isEmpty()).isTrue();
    }

    private Coach generateCoach(int coachNumber, int numOfSeats) {
        SeatType[] types = SeatType.values();
        List<Seat> seats = new ArrayList<>(numOfSeats);
        for (int i = 0; i <numOfSeats; i++) {
            seats.add(Seat.builder().coachNumber(coachNumber).seatNumber(i + 1).type(types[i % types.length]).build());
        }
        Coach coach = Coach.builder().coachNumber(coachNumber).seats(seats).build();
        return coach;
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

}
