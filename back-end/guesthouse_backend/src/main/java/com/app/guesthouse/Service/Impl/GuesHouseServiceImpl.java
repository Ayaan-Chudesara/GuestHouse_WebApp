package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.GuestHouseDTO;
import com.app.guesthouse.Entity.GuestHouse;
import com.app.guesthouse.Repository.GuestHouseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GuesHouseServiceImpl {

    @Autowired
    private GuestHouseRepo guestHouseRepo;

    public GuestHouseDTO createGuestHouse(GuestHouseDTO dto) {
        GuestHouse guestHouse = new GuestHouse();
        guestHouse.setName(dto.getName());
        guestHouse.setLocation(dto.getLocation());
        GuestHouse saved = guestHouseRepo.save(guestHouse);
        dto.setId(saved.getId());
        return dto;
    }

    public List<GuestHouseDTO> getAllGuestHouses() {
        return guestHouseRepo.findAll().stream().map(g ->
                new GuestHouseDTO(g.getId(), g.getName(), g.getLocation())
        ).collect(Collectors.toList());
    }

    public GuestHouseDTO getGuestHouseById(Long id) {
        Optional<GuestHouse> optional = guestHouseRepo.findById(id);
        return optional.map(g -> new GuestHouseDTO(g.getId(), g.getName(), g.getLocation())).orElse(null);
    }

    public GuestHouseDTO updateGuestHouse(Long id, GuestHouseDTO dto) {
        Optional<GuestHouse> optional = guestHouseRepo.findById(id);
        if (optional.isPresent()) {
            GuestHouse g = optional.get();
            g.setName(dto.getName());
            g.setLocation(dto.getLocation());
            guestHouseRepo.save(g);
            return new GuestHouseDTO(g.getId(), g.getName(), g.getLocation());
        }
        return null;
    }

    public boolean deleteGuestHouse(Long id) {
        if (guestHouseRepo.existsById(id)) {
            guestHouseRepo.deleteById(id);
            return true;
        }
        return false;
    }
}
