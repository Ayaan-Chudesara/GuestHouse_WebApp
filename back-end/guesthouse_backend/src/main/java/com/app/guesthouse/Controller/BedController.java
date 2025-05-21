package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.BedDTO;
import com.app.guesthouse.Service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        try {
            return ResponseEntity.ok(bedService.getAllBeds());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BedDTO> getBedById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bedService.getBedById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<BedDTO> createBed(@RequestBody BedDTO bedDTO) {
        try {
            return ResponseEntity.ok(bedService.createBed(bedDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BedDTO> updateBed(@PathVariable Long id, @RequestBody BedDTO bedDTO) {
        try {
            return ResponseEntity.ok(bedService.updateBed(id, bedDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBed(@PathVariable Long id) {
        try {
            bedService.deleteBed(id);
            return ResponseEntity.ok("Bed deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BedDTO>> getBedsByRoomId(@PathVariable Long roomId) {
        try {
            return ResponseEntity.ok(bedService.getBedsByRoomId(roomId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
