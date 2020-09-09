package com.transport.irctc.TicketBooking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.transport.irctc.TicketBooking.Models.Coach;
import com.transport.irctc.TicketBooking.Models.Seat;
import com.transport.irctc.TicketBooking.Models.SeatType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// This is just a program used for randomly generating data and writing it into the file
public class TrainCoachAdditionEventsGenerator {
    private static int NUM_OF_RECORDS=1000;
    private static Random random=new Random();
    private static ObjectWriter objectWriter = new ObjectMapper().writer();
    private static SeatType[] seatTypes=SeatType.values();
    public static void main(String[] args) throws IOException {
        String destination = "C:\\Users\\cneer\\Desktop";
        String filePath = Path.of(destination, "CoachDetails.csv").toString();
        FileWriter out = new FileWriter(filePath,false);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            for (int i = 0; i < NUM_OF_RECORDS; i++) {
                printer.printRecord(getCoachListJSON());
            }
        }
    }

    private static String getCoachListJSON() throws JsonProcessingException {
        // coach list
        int numOfCoaches = 10;
        int numOfSeats = 60;
        List<Coach> coachList = new ArrayList<Coach>();
        for (int i = 1; i <= numOfCoaches; i++) {
            List<Seat> seats = new ArrayList<>();
            for (int j = 1; j <= numOfSeats; j++) {
                seats.add(Seat.builder().seatNumber(j).coachNumber(i).type(seatTypes[j % seatTypes.length]).build());
            }
            Coach coach = Coach.builder().coachNumber(i).seats(seats).build();
            coachList.add(coach);
        }
        return objectWriter.writeValueAsString(coachList);
    }
}
