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

}
