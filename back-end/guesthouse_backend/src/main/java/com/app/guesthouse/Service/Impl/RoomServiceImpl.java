package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.RoomDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.GuestHouse;
import com.app.guesthouse.Entity.Room;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.GuestHouseRepo;
import com.app.guesthouse.Repository.RoomRepo;
import com.app.guesthouse.Service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepo roomRepository;

    @Autowired
    private GuestHouseRepo guestHouseRepository;

    @Autowired
    private BedRepo bedRepo;


    public RoomDTO createRoom(RoomDTO roomDTO) {
        Room room = new Room();
        room.setRoomNo(roomDTO.getRoomNo());
        room.setRoomType(roomDTO.getRoomType());
        room.setNumberOfBeds(roomDTO.getNumberOfBeds());
        room.setPricePerNight(roomDTO.getPricePerNight());

        GuestHouse guestHouse = guestHouseRepository.findById(roomDTO.getGuestHouseId())
                .orElseThrow(() -> new EntityNotFoundException("GuestHouse not found"));

        room.setGuestHouse(guestHouse);

        Room savedRoom = roomRepository.save(room);
        return mapToDTO(savedRoom);
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        return mapToDTO(room);
    }

    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        room.setRoomNo(roomDTO.getRoomNo());
        room.setRoomType(roomDTO.getRoomType());
        room.setNumberOfBeds(roomDTO.getNumberOfBeds());
        room.setPricePerNight(roomDTO.getPricePerNight());

        GuestHouse guestHouse = guestHouseRepository.findById(roomDTO.getGuestHouseId())
                .orElseThrow(() -> new EntityNotFoundException("GuestHouse not found"));
        room.setGuestHouse(guestHouse);

        Room updated = roomRepository.save(room);
        return mapToDTO(updated);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found");
        }
        roomRepository.deleteById(id);
    }

    private RoomDTO mapToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setRoomNo(room.getRoomNo());
        dto.setRoomType(room.getRoomType());
        dto.setGuestHouseId(room.getGuestHouse().getId());
        dto.setGuestHouseName(room.getGuestHouse().getName());
        dto.setNumberOfBeds(room.getNumberOfBeds());
        dto.setPricePerNight(room.getPricePerNight());
        return dto;
    }

    @Override
    public List<String> getDistinctRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<RoomDTO> searchAvailableRooms(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Long guestHouseId,
            String roomType,
            Integer numberOfGuests) {

        List<Bed> availableBeds = bedRepo.findAvailableBedsByCriteria(
            guestHouseId,
            roomType,
            numberOfGuests != null ? numberOfGuests : 1,
            checkInDate,
            checkOutDate
        );

        List<Room> availableRooms = availableBeds.stream()
            .map(Bed::getRoom)
            .distinct()
            .collect(Collectors.toList());

        return availableRooms.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
}
