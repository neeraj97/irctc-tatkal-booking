package com.transport.irctc.TicketBooking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.transport.irctc.TicketBooking.Models.BookingDetails;
import com.transport.irctc.TicketBooking.Models.PassengerDetails;
import com.transport.irctc.TicketBooking.Models.SeatType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// This is just a program used for randomly generating data
public class BookingDetailsGenerator {
    public static int NUM_OF_RECORDS=100000;
    public static Random random=new Random();
    public static SeatType preferences[]= SeatType.values();
    public static String[] names={"Joe","Ronald","Chris","Chuck","Lovren","Drake","Nick","Stera","Kaira","Abhi","James","George","Williams",
                                    "Mohan D'Souza","Sergio","Thiago","David","Micheal","Ramaz","Romavic","Mavich"};
    public static String[] boardStationCode={"ATP","HYD","YPR","KSR","CHN","NLR","GTR","AMZ","IDR","GWL","BMH","CBT","BDT","PUN","AMD","GDN"};
    public static String[] destinationStationCode={"NYC","CLF","MDW","NBK","PSN","ALS","WAL","LPZ","BYN","MA","BRC","LSP","MDR","MDV","YUT","VAN",
                                                    "BCM"};
    public static ObjectWriter objectWriter= new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static void main(String[] args) throws IOException {

        String destination = "C:\\Users\\cneer\\Desktop";
        String[] HEADERS = new String[]{"BookingDetails"};
        String filePath = Path.of(destination, "BookingDetails.csv").toString();
        FileWriter out = new FileWriter(filePath,true);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            for (int i = 0; i < NUM_OF_RECORDS; i++) {
                printer.printRecord(getBookingDetailsJSON());
            }
        }
    }

    // This Generates Random BookingDetails
    public static String getBookingDetailsJSON() throws JsonProcessingException {
        // create Passenger List
        List<PassengerDetails> passengerlist = new ArrayList<>();
        int numOfPassengers = random.nextInt(6) + 1;
        for (int i = 0; i < numOfPassengers; i++) {
            PassengerDetails passengerDetails = PassengerDetails.builder()
                    .age(random.nextInt(100) + 1)
                    // simulating the preference for half of the passengers
                    .preference(random.nextBoolean()?preferences[random.nextInt(preferences.length)]:null)
                    .name(names[random.nextInt(names.length)])
                    .build();
            passengerlist.add(passengerDetails);
        }

        // create booking details
        BookingDetails bookingDetails = BookingDetails.builder()
                .trainNumber(random.nextInt(1000))
                // simulating 'true' for 10% of the customers
                .bookOnlyIfSeatsAreAvailable(random.nextInt(10)!=0)
                // simulating 'true' for 20% of the customers
                .bookOnlyPreferredSeatTypes(random.nextInt(5)!=0)
                .boardStationCode(boardStationCode[random.nextInt(boardStationCode.length)])
                .destinationStationCode(destinationStationCode[random.nextInt(destinationStationCode.length)])
                .passengerDetails(passengerlist)
                .build();
        return objectWriter.writeValueAsString(bookingDetails);
    }

}
