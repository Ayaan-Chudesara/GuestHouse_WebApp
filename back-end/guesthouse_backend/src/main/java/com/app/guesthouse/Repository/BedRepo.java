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

    @Query(value = "SELECT b.* FROM beds b " +
            "JOIN rooms r ON r.id = b.room_id " +
            "JOIN guest_houses gh ON gh.id = r.guest_house_id " +
            "WHERE b.status = 'AVAILABLE' " +
            "AND gh.id = :guestHouseId " +
            "AND r.room_type = :roomType " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM bookings bo " +
            "   WHERE bo.bed_id = b.id " +
            "   AND bo.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " +
            "   AND (" +
            "       bo.booking_date < :checkOutDate " +
            "       AND DATE_ADD(bo.booking_date, INTERVAL bo.duration_days DAY) > :checkInDate" +
            "   )" +
            ")", nativeQuery = true)
    List<Bed> findAvailableBedsByCriteria(
            @Param("guestHouseId") Long guestHouseId,
            @Param("roomType") String roomType,
            @Param("roomCapacity") Integer roomCapacity,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    @Query(value = "SELECT b.* FROM beds b " +
            "WHERE b.room_id = :roomId " +
            "AND b.status = 'AVAILABLE' " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM bookings bo " +
            "   WHERE bo.bed_id = b.id " +
            "   AND bo.status IN ('PENDING', 'APPROVED', 'CHECKED_IN') " +
            "   AND (" +
            "       bo.booking_date < :checkOutDate " +
            "       AND DATE_ADD(bo.booking_date, INTERVAL bo.duration_days DAY) > :checkInDate" +
            "   )" +
            ") LIMIT 1", nativeQuery = true)
    List<Bed> findFirstAvailableBedInRoomForDates(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
