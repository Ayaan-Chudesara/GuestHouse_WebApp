package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BedRepo extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomId(Long roomId);

    long countByStatus(Bed.Status status);


    @Query("SELECT b FROM Bed b " +
            "JOIN b.room r " +
            "JOIN r.guestHouse gh " +
            "WHERE gh.id = :guestHouseId " +
            "AND r.roomType = :roomType " +
            "AND b.status = 'AVAILABLE'")
    List<Bed> findAvailableBeds(
            @Param("guestHouseId") Long guestHouseId,
            @Param("roomType") String roomType,
            @Param("checkInDate") LocalDate checkIn,
            @Param("checkOutDate") LocalDate checkOut,
            @Param("numberOfBeds") int numberOfBeds
    );

    @Query("SELECT b FROM Bed b " +
            "JOIN b.room r " +
            "JOIN r.guestHouse gh " +
            "WHERE b.status = 'AVAILABLE' " + // Only consider beds explicitly marked as AVAILABLE
            "AND gh.id = :guestHouseId " +
            "AND r.roomType = :roomType " +
            "AND r.numberOfBeds >= :roomCapacity " + // Ensure room has at least the requested capacity
            "AND b.id NOT IN (" + // Exclude beds that have overlapping active bookings
            "   SELECT bo.bed.id FROM Booking bo " +
            "   WHERE bo.bed.id = b.id " +
            "   AND bo.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " + // Consider active booking statuses
            "   AND (" + // Overlap condition:
            "       (bo.bookingDate <= :checkOutDate AND FUNCTION('DATE_ADD', bo.bookingDate, 'DAY', bo.durationDays) > :checkInDate)" +
            "   )" +
            ")")
    List<Bed> findAvailableBedsByCriteria(
            @Param("guestHouseId") Long guestHouseId,
            @Param("roomType") String roomType,
            @Param("roomCapacity") Integer roomCapacity,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate); // This is the end date of the search, it will be used in comparison with other bookings


    @Query("SELECT b FROM Bed b WHERE b.room.id = :roomId " +
            "AND b.status = 'AVAILABLE' " + // Ensure the bed is marked as AVAILABLE
            "AND b.id NOT IN (" + // Exclude beds that have active overlapping bookings
            "   SELECT bo.bed.id FROM Booking bo WHERE bo.bed.id = b.id " +
            "   AND bo.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " +
            "   AND (" + // Overlap condition:
            "       (bo.bookingDate < :checkOutDate AND FUNCTION('DATE_ADD', bo.bookingDate, 'DAY', bo.durationDays) > :checkInDate)" +
            "   )" +
            ")")
    List<Bed> findFirstAvailableBedInRoomForDates(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

}
