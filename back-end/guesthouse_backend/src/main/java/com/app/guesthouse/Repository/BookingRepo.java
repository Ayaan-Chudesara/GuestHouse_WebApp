package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    List<Booking> findByStatus(Booking.Status status);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);

    long countByStatus(Booking.Status status);

    // Your existing query to find bookings that overlap with a given date range for a specific bed
    @Query("SELECT b FROM Booking b " +
            "WHERE b.bed.id = :bedId " +
            "AND b.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " + // Consider relevant active statuses
            "AND (" +
            "    (b.bookingDate < :endDate AND FUNCTION('DATE_ADD', b.bookingDate, 'DAY', b.durationDays) > :startDate)" +
            ")")
    List<Booking> findOverlappingBookingsForBed(
            @Param("bedId") Long bedId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // --- NEW METHOD ADDED: findOverlappingBookingsForBedExcludingCurrent ---
    // Finds active overlapping bookings for a given bed, excluding a specific booking ID (the one being deleted/updated)
    @Query("SELECT bo FROM Booking bo WHERE bo.bed.id = :bedId " +
            "AND bo.id != :excludedBookingId " + // Exclude the current booking being processed
            "AND bo.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " + // Consider active statuses
            "AND (" + // Overlap condition:
            "    (bo.bookingDate < :newBookingCheckOutDate AND FUNCTION('DATE_ADD', bo.bookingDate, 'DAY', bo.durationDays) > :newBookingCheckInDate)" +
            ")")
    List<Booking> findOverlappingBookingsForBedExcludingCurrent(
            @Param("bedId") Long bedId,
            @Param("newBookingCheckInDate") LocalDate newBookingCheckInDate,
            @Param("newBookingCheckOutDate") LocalDate newBookingCheckOutDate,
            @Param("excludedBookingId") Long excludedBookingId
    );
    // --- END NEW METHOD ---
}