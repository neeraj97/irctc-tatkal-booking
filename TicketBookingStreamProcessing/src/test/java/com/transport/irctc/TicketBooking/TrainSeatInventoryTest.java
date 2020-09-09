package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Models.Coach;
import com.transport.irctc.TicketBooking.Models.Seat;
import com.transport.irctc.TicketBooking.Models.SeatType;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TrainSeatInventoryTest {

    private TrainSeatInventory testClass=new TrainSeatInventory();

    @Test
    public void givenSeats_WhenAddSeats_ThenSuccessfullyAddSeats() {
        // Arrange
        Seat seat =Seat.builder().coachNumber(1).seatNumber(1).type(SeatType.LOWER).build();

        // Act
        testClass.addSeats(Collections.singletonList(seat));

        // Assert
        Assertions.assertThat(testClass.getTotalAvailableSeatsCount()).isEqualTo(1);
    }

    @Test
    public void givenCoaches_WhenAddCoaches_ThenSuccessfullyAddCoaches() {
        // Arrange
        int numOfSeats=60;
        int numOfCoaches=2;
        ArrayList<Coach> coachList=new ArrayList<>();
        for(int i=1;i<=numOfCoaches;i++){
            coachList.add(generateCoach(i,numOfSeats));
        }

        // Act
        testClass.addCoaches(coachList);

        // Assert
        Assertions.assertThat(testClass.getTotalAvailableSeatsCount()).isEqualTo(numOfCoaches*numOfSeats);
    }

    @Test
    public void givenAvailableSeats_WhenGetAndAllocateAnySeat_ThenReturnAllocatedSeatLocation(){
        // Arrange
        Seat seat =Seat.builder().coachNumber(1).seatNumber(1).type(SeatType.LOWER).build();
        testClass.addSeats(Collections.singletonList(seat));

        // Act
        Optional<Seat> allocatedSeat = testClass.allocateAnySeat();

        // Assert
        Assertions.assertThat(allocatedSeat).isNotEmpty();
        Assertions.assertThat(allocatedSeat.get().getCoachNumber()).isEqualTo(seat.getCoachNumber());
        Assertions.assertThat(allocatedSeat.get().getSeatNumber()).isEqualTo(seat.getSeatNumber());
    }

    @Test
    public void givenNoAvailableSeats_WhenGetAndAllocateAnySeat_ThenReturnNoSeatLocation(){
        // Arrange

        // Act
        Optional<Seat> allocatedSeat = testClass.allocateAnySeat();

        // Assert
        Assertions.assertThat(allocatedSeat).isEmpty();
    }

    @Test
    public void givenAvailableSeatsOfGivenSeatType_WhenGetAndAllocateSeatOfGivenSeatType_ThenReturnAllocatedSeatLocation(){
        // Arrange
        SeatType givenSeatType= SeatType.LOWER;
        Seat seat = Seat.builder().coachNumber(1).seatNumber(1).type(givenSeatType).build();
        testClass.addSeats(Collections.singletonList(seat));

        // Act
        Optional<Seat> allocatedSeat = testClass.allocateSeatOfGivenSeatType(givenSeatType);

        // Assert
        Assertions.assertThat(allocatedSeat).isNotEmpty();
        Assertions.assertThat(allocatedSeat.get().getCoachNumber()).isEqualTo(seat.getCoachNumber());
        Assertions.assertThat(allocatedSeat.get().getSeatNumber()).isEqualTo(seat.getSeatNumber());
    }

    @Test
    public void givenNoAvailableSeatsOfGivenSeatType_WhenGetAndAllocateSeatOfGivenSeatType_ThenReturnAllocatedSeatLocation(){
        // Arrange
        SeatType givenSeatType= SeatType.LOWER;
        Seat seat = Seat.builder().coachNumber(1).seatNumber(1).type(SeatType.SIDE_LOWER).build();
        testClass.addSeats(Collections.singletonList(seat));

        // Act
        Optional<Seat> allocatedSeat = testClass.allocateSeatOfGivenSeatType(givenSeatType);

        // Assert
        Assertions.assertThat(allocatedSeat).isEmpty();
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
}
