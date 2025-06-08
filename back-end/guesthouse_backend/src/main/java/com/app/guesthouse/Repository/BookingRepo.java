package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    List<Booking> findByStatus(Booking.Status status);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);

    long countByStatus(Booking.Status status);

    @Query(value = "SELECT b.* FROM bookings b " +
            "WHERE b.bed_id = :bedId " +
            "AND b.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " +
            "AND NOT (" +
            "    b.booking_date >= :endDate " +
            "    OR DATE_ADD(b.booking_date, INTERVAL b.duration_days DAY) <= :startDate" +
            ")", nativeQuery = true)
    List<Booking> findOverlappingBookingsForBed(
            @Param("bedId") Long bedId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    @Query("SELECT bo FROM Booking bo WHERE bo.bed.id = :bedId " +
            "AND bo.id != :excludedBookingId " +
            "AND bo.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " +
            "AND (" +
            "    (bo.bookingDate < :newBookingCheckOutDate AND FUNCTION('DATE_ADD', bo.bookingDate, 'DAY', bo.durationDays) > :newBookingCheckInDate)" +
            ")")
    List<Booking> findOverlappingBookingsForBedExcludingCurrent(
            @Param("bedId") Long bedId,
            @Param("newBookingCheckInDate") LocalDate newBookingCheckInDate,
            @Param("newBookingCheckOutDate") LocalDate newBookingCheckOutDate,
            @Param("excludedBookingId") Long excludedBookingId
    );

    @Query(value = "SELECT b.* FROM bookings b " +
           "JOIN beds bed ON b.bed_id = bed.id " +
           "JOIN rooms r ON bed.room_id = r.id " +
           "WHERE (:guestHouseId IS NULL OR r.guest_house_id = :guestHouseId) " +
           "AND (:roomType IS NULL OR r.room_type = :roomType) " +
           "AND (:checkInDate IS NULL OR b.booking_date >= :checkInDate) " +
           "AND (:checkOutDate IS NULL OR DATE_ADD(b.booking_date, INTERVAL b.duration_days DAY) <= :checkOutDate) " +
           "AND (:status IS NULL OR b.status = :status)", 
           nativeQuery = true)
    List<Booking> findFilteredBookings(
        @Param("guestHouseId") Long guestHouseId,
        @Param("roomType") String roomType,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate,
        @Param("status") String status
    );
}