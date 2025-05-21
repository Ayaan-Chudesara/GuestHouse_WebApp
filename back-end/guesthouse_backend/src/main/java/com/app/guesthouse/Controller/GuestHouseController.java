package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.GuestHouseDTO;
import com.app.guesthouse.Service.GuestHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guesthouses")
public class GuestHouseController {

    @Autowired
    private GuestHouseService guestHouseService;

    @PostMapping
    public GuestHouseDTO create(@RequestBody GuestHouseDTO dto) {
        return guestHouseService.createGuestHouse(dto);
    }

    @GetMapping
    public List<GuestHouseDTO> getAll() {
        return guestHouseService.getAllGuestHouses();
    }

    @GetMapping("/{id}")
    public GuestHouseDTO getById(@PathVariable Long id) {
        return guestHouseService.getGuestHouseById(id);
    }

    @PutMapping("/{id}")
    public GuestHouseDTO update(@PathVariable Long id, @RequestBody GuestHouseDTO dto) {
        return guestHouseService.updateGuestHouse(id, dto);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return guestHouseService.deleteGuestHouse(id);
    }
}
