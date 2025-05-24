package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.AdminBookingRequestDTO;
import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Service.AdminService;
import com.app.guesthouse.Service.BookingService;
import com.app.guesthouse.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bookings/{id}/approve")
    public ResponseEntity<String> approveBooking(@PathVariable Long id) {
        try {
            adminService.approveBooking(id);
            return ResponseEntity.ok("Booking approved.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    private ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.saveUser(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    private ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.updateUser(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/dashboard/stats")

    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/scheduler")

    public ResponseEntity<List<BookingDTO>> getSchedulerData(@RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(adminService.getSchedulerData(start, end));
    }

    @GetMapping("/dashboard/total-beds")

    public ResponseEntity<Integer> getTotalBeds() {
        return ResponseEntity.ok(adminService.getTotalBeds());
    }


    @PostMapping("/bookings")
    public ResponseEntity<String> createBookingByAdmin(@RequestBody AdminBookingRequestDTO request) {
        bookingService.createBookingAsAdmin(request);
        return ResponseEntity.ok("Booking created successfully");
    }

}
