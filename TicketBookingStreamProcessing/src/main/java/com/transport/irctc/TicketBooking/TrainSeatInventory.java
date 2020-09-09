package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Models.Coach;
import com.transport.irctc.TicketBooking.Models.Seat;
import com.transport.irctc.TicketBooking.Models.SeatType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


public class TrainSeatInventory {

    @Getter
    @Setter
    private Map<SeatType, ArrayDeque<Seat>> availableSeats;

    @Getter
    @Setter
    private int totalAvailableSeatsCount = 0;

    private int totalSeatTypes;

    private SeatType seatTypes[];

    public TrainSeatInventory() {
        seatTypes = SeatType.values();
        totalSeatTypes = seatTypes.length;
        availableSeats = new HashMap<>();
        for (int i = 0; i < totalSeatTypes; i++)
            availableSeats.put(seatTypes[i], new ArrayDeque<>());
    }

    public void addCoach(Coach coach) {
        List<Seat> coachSeats = coach.getSeats();
        if (availableSeats == null)
            return;

        for (Seat seat : coachSeats) {
            availableSeats.get(seat.getType()).add(seat);
            totalAvailableSeatsCount++;
        }
    }

    public void addCoaches(List<Coach> coachList) {
        coachList.forEach(coach -> addCoach(coach));
    }

    public void addSeats(List<Seat> seats) {
        for (Seat seat : seats) {
            availableSeats.get(seat.getType()).add(seat);
            totalAvailableSeatsCount++;
        }
    }

    // allocates the smallest available seats .. see compareSeats() for comparing seats
    public Optional<Seat> allocateAnySeat() {
        if (totalAvailableSeatsCount == 0)
            return Optional.empty();

        SeatType seatType=null;
        Seat seat = null;

        for (int i = 0; i < totalSeatTypes; i++) {
            if (!availableSeats.get(seatTypes[i]).isEmpty()) {
                Seat temp = availableSeats.get(seatTypes[i]).peekFirst();
                if (compareSeats(temp, seat) < 0) {
                    seat = temp;
                    seatType = seatTypes[i];
                }
            }
        }
        if (seat == null)
            return Optional.empty();
        availableSeats.get(seatType).pollFirst();
        totalAvailableSeatsCount--;
        return Optional.of(seat);
    }

    public Optional<Seat> allocateSeatOfGivenSeatType(SeatType seatType) {
        if (availableSeats.get(seatType).isEmpty())
            return Optional.empty();

        Seat seatLocation = availableSeats.get(seatType).pollFirst();
        totalAvailableSeatsCount--;
        return Optional.of(seatLocation);
    }

    public int getNumberOfSeatsAvailableForGivenSeatType(SeatType seatType) {
        return availableSeats.get(seatType).size();
    }

    private int compareSeats(Seat temp, Seat seatLocation) {
        if (seatLocation == null)
            return -1;
        if (temp == null)
            return 1;
        if (temp.getCoachNumber() == seatLocation.getCoachNumber())
            return temp.getSeatNumber() - seatLocation.getSeatNumber();
        return temp.getCoachNumber() - seatLocation.getCoachNumber();
    }

}