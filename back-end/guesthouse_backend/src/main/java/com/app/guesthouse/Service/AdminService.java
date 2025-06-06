package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.BookingDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdminService {
    List<BookingDTO> getAllBookings();
    BookingDTO updateBookingStatus(Long bookingId, String status);
    BookingDTO rejectBooking(Long bookingId);
    BookingDTO approveBooking(Long bookingId);
    List<BookingDTO> getPendingBookings();

    Map<String, Object> getDashboardStats();
    List<BookingDTO> getSchedulerData(String start, String end);
    Integer getTotalBeds();


    void deleteBooking(Long id);

    BookingDTO updateBooking(Long id, BookingDTO bookingDTO);

    List<BookingDTO> getFilteredBookings(
            Long guestHouseId,
            String roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String status);
}
