package com.transport.irctc.TicketBooking.Repositories;

import com.transport.irctc.TicketBooking.Models.BookingDetails;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingDetailsRepository extends CassandraRepository<BookingDetails,String> {
}
