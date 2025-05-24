package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.AdminBookingRequestDTO;
import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingDTO bookingDTO);

    List<BookingDTO> getAllBookings();

    BookingDTO getBookingById(Long id);

    void deleteBooking(Long id);

    List<BookingDTO> getBookingsByUser(Long userId);

    BookingDTO updateBookingStatus(Long id, Booking.Status status);



    List<BookingDTO> getBookingsBetweenDates(LocalDate start, LocalDate end);

    void createBookingAsAdmin(AdminBookingRequestDTO request);
}
