package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Booking;

import java.awt.print.Book;
import java.util.List;

public interface AdminService {
    List<BookingDTO> getAllBookings();
    BookingDTO updateBookingStatus(Long bookingId, String status);
    BookingDTO rejectBooking(Long bookingId);
    BookingDTO approveBooking(Long bookingId);
    List<BookingDTO> getPendingBookings();


}
