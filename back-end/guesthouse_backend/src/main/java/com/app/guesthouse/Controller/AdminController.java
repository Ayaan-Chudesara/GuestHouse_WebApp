package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Service.AdminService;
import com.app.guesthouse.Service.UserService;
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

    @Autowired
    private UserService userService;

    @GetMapping("/allBookings")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        try {
            return ResponseEntity.ok(adminService.getAllBookings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/allPendingBookings")
    public ResponseEntity<List<BookingDTO>> getAllPendingBookings() {
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

    @GetMapping("/all")
    private ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    private ResponseEntity<UserDTO> getUserById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    private ResponseEntity deleteUserById(@RequestParam Long id){
        try{
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save")
    private ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.saveUser(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{id}")
    private ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.updateUser(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
