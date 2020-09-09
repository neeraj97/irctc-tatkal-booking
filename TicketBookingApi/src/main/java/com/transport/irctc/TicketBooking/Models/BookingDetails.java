package com.transport.irctc.TicketBooking.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.cassandra.core.mapping.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Getter
@Setter
@Table("booking_details")
public class BookingDetails {

    @PrimaryKey("paymentorderid")
    public String paymentOrderId;

    @Column("bookingid")
    private String bookingId;

    @NotNull
    @Column("trainnumber")
    private Integer trainNumber;

    @NotNull
    @Column("boardstationcode")
    private String boardStationCode;

    @NotNull
    @Column("destinationstationcode")
    private String destinationStationCode;

    @NotNull
    @Column("passengerdetails")
    @Frozen
    @CassandraType(type = CassandraType.Name.LIST,typeArguments = CassandraType.Name.UDT,userTypeName = "passenger_details")
    private List<PassengerDetails> passengerDetails;

    @Column("bookonlyifpreferredseatsareavailable")
    private boolean bookOnlyPreferredSeatTypes;

    @Column("bookonlyifseatsareavailable")
    private boolean bookOnlyIfSeatsAreAvailable;

}
