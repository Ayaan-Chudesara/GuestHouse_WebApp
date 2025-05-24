package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.RoomDTO;
import com.app.guesthouse.Entity.GuestHouse;
import com.app.guesthouse.Entity.Room;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

public interface RoomService {

    public RoomDTO createRoom(RoomDTO roomDTO) ;

    // 📋 Get All Rooms
    public List<RoomDTO> getAllRooms() ;

    // 📄 Get Room by ID
    public RoomDTO getRoomById(Long id) ;

    // ✏️ Update Room
    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) ;

    // ❌ Delete Room
    public void deleteRoom(Long id) ;

    List<String> getDistinctRoomTypes();
}
