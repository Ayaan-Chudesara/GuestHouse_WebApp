package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.GuestHouseDTO;

import java.util.List;

public interface GuestHouseService {
    GuestHouseDTO createGuestHouse(GuestHouseDTO dto);

    List<GuestHouseDTO> getAllGuestHouses();

    GuestHouseDTO getGuestHouseById(Long id);

    GuestHouseDTO updateGuestHouse(Long id, GuestHouseDTO dto);

    boolean deleteGuestHouse(Long id);
}
