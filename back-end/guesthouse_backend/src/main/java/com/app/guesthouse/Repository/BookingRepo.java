package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(Booking.Status status);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);

}
