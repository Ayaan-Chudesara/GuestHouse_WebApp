package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.BedDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Room;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.RoomRepo;
import com.app.guesthouse.Service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
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
        if (bed.getRoom() != null) {
            dto.setRoomId(bed.getRoom().getId());
            dto.setRoomNo(bed.getRoom().getRoomNo());
        }
        return dto;
    }

    public Bed mapToEntity(BedDTO dto) {
        Bed bed = new Bed();
        if (dto.getId() != null) {
            bed.setId(dto.getId());
        }
        bed.setBedNo(dto.getBedNo());
        bed.setStatus(dto.getStatus());
        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + dto.getRoomId()));
            bed.setRoom(room);
        } else {
            throw new IllegalArgumentException("Room ID must be provided for a bed.");
        }
        return bed;
    }

    @Override
    public List<BedDTO> getAllBeds() {
        return bedRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public BedDTO getBedById(Long id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bed not found with ID: " + id));
        return mapToDTO(bed);
    }


    @Override
    @Transactional
    public BedDTO createBed(BedDTO dto) {
        Bed bed = mapToEntity(dto);
        return mapToDTO(bedRepository.save(bed));
    }

    @Override
    @Transactional
    public BedDTO updateBed(Long id, BedDTO dto) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bed not found with ID: " + id));

        bed.setBedNo(dto.getBedNo());
        if (dto.getStatus() != null) {
            bed.setStatus(dto.getStatus());
        }
        if (dto.getRoomId() != null && !dto.getRoomId().equals(bed.getRoom() != null ? bed.getRoom().getId() : null)) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + dto.getRoomId()));
            bed.setRoom(room);
        }
        return mapToDTO(bedRepository.save(bed));
    }

    @Override
    @Transactional
    public void deleteBed(Long id) {
        if (!bedRepository.existsById(id)) {
            throw new NoSuchElementException("Bed not found with ID: " + id);
        }
        bedRepository.deleteById(id);
    }

    public List<BedDTO> getBedsByRoomId(Long roomId) {
        return bedRepository.findByRoomId(roomId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}