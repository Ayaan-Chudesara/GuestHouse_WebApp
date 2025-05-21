package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.BedDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Room;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.RoomRepo;
import com.app.guesthouse.Service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BedServiceImpl implements BedService {
    @Autowired
    private BedRepo bedRepository;

    @Autowired
    private RoomRepo roomRepository;

    public BedDTO mapToDTO(Bed bed) {
        BedDTO dto = new BedDTO();
        dto.setId(bed.getId());
        dto.setBedNo(bed.getBedNo());
        dto.setStatus(bed.getStatus());
        dto.setRoomId(bed.getRoom().getId());
        dto.setRoomNo(bed.getRoom().getRoomNo());
        return dto;
    }

    public Bed mapToEntity(BedDTO dto) {
        Bed bed = new Bed();
        bed.setId(dto.getId());
        bed.setBedNo(dto.getBedNo());
        bed.setStatus(dto.getStatus());
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        bed.setRoom(room);
        bed.setRoom(room);
        return bed;
    }

    public List<BedDTO> getAllBeds() {
        return bedRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public BedDTO getBedById(Long id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bed not found"));
        return mapToDTO(bed);
    }

    public BedDTO createBed(BedDTO dto) {
        Bed bed = mapToEntity(dto);
        return mapToDTO(bedRepository.save(bed));
    }

    public BedDTO updateBed(Long id, BedDTO dto) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bed not found"));
        bed.setBedNo(dto.getBedNo());
        bed.setStatus(dto.getStatus());
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        bed.setRoom(room);
        return mapToDTO(bedRepository.save(bed));
    }

    public void deleteBed(Long id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bed not found"));
        bedRepository.delete(bed);
    }

    public List<BedDTO> getBedsByRoomId(Long roomId) {
        return bedRepository.findByRoomId(roomId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


}


