package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/allBookings")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        try {
            return ResponseEntity.ok(adminService.getAllBookings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/allPendingBookings")
    public ResponseEntity<List<BookingDTO>> getAllUsers() {
        try {
            return ResponseEntity.ok(adminService.getPendingBookings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/bookings/{id}/approve")
    public ResponseEntity<String> approveBooking(@PathVariable Long id) {
        try {
            adminService.approveBooking(id);
            return ResponseEntity.ok("Booking approved.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/bookings/{id}/reject")
    public ResponseEntity<String> rejectBooking(@PathVariable Long id) {
        try {
            adminService.rejectBooking(id);
            return ResponseEntity.ok("Booking rejected.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/bookings/{id}/updateStatus/{status}")
    public ResponseEntity<BookingDTO> updateBookingStatus(@PathVariable Long id, @PathVariable String status){
        try {
            return ResponseEntity.ok(adminService.updateBookingStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
