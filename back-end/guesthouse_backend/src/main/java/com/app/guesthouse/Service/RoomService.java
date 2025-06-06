package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.RoomDTO;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    RoomDTO createRoom(RoomDTO roomDTO) ;

    List<RoomDTO> getAllRooms() ;

    RoomDTO getRoomById(Long id) ;

    RoomDTO updateRoom(Long id, RoomDTO roomDTO) ;

    void deleteRoom(Long id) ;

    List<String> getDistinctRoomTypes();

    List<RoomDTO> searchAvailableRooms(
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long guestHouseId,
        String roomType,
        Integer numberOfGuests);
}
