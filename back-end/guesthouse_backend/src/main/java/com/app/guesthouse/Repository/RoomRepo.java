package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepo extends JpaRepository<Room, Long > {
    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();

    List<Room> findByGuestHouseIdAndRoomType(Long guestHouseId, String roomType);
}
