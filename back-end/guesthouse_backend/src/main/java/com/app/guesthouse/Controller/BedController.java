package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.BedDTO;
import com.app.guesthouse.Service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beds")
public class BedController {

    @Autowired
    private BedService bedService;

    @GetMapping
    public ResponseEntity<List<BedDTO>> getAllBeds() {
        return ResponseEntity.ok(bedService.getAllBeds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BedDTO> getBedById(@PathVariable Long id) {
        return ResponseEntity.ok(bedService.getBedById(id));
    }

    @PostMapping
    public ResponseEntity<BedDTO> createBed(@RequestBody BedDTO bedDTO) {
        return ResponseEntity.ok(bedService.createBed(bedDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BedDTO> updateBed(@PathVariable Long id, @RequestBody BedDTO bedDTO) {
        return ResponseEntity.ok(bedService.updateBed(id, bedDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBed(@PathVariable Long id) {
        bedService.deleteBed(id);
        return ResponseEntity.ok("Bed deleted successfully");
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BedDTO>> getBedsByRoomId(@PathVariable Long roomId) {
        return ResponseEntity.ok(bedService.getBedsByRoomId(roomId));
    }
}
