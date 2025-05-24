package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <--- ADD THIS IMPORT
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> { // <--- ADD THIS INTERFACE EXTENSION
    List<Booking> findByStatus(Booking.Status status);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);

    long countByStatus(Booking.Status status);

    // Query to find bookings that overlap with a given date range for a specific bed
    @Query("SELECT b FROM Booking b " +
            "WHERE b.bed.id = :bedId " +
            "AND b.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " + // Consider relevant active statuses
            "AND (" +
            "    (b.bookingDate <= :endDate AND FUNCTION('DATE_ADD', b.bookingDate, 'DAY', b.durationDays) > :startDate)" +
            ")")
    List<Booking> findOverlappingBookingsForBed(
            @Param("bedId") Long bedId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}