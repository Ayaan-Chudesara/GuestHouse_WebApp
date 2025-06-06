package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.AdminBookingRequestDTO;
import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.DTO.GuestHouseDTO;
import com.app.guesthouse.Service.AdminService;
import com.app.guesthouse.Service.BookingService;
import com.app.guesthouse.Service.UserService;
import com.app.guesthouse.Service.GuestHouseService;
import com.app.guesthouse.Service.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final BookingService bookingService;
    private final GuestHouseService guestHouseService;
    private final RoomService roomService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService,
                           BookingService bookingService, GuestHouseService guestHouseService,
                           RoomService roomService) {
        this.adminService = adminService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.guestHouseService = guestHouseService;
        this.roomService = roomService;
    }

    @GetMapping("/allBookings")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @GetMapping("/allPendingBookings")
    public ResponseEntity<List<BookingDTO>> getAllPendingBookings() {
        return ResponseEntity.ok(adminService.getPendingBookings());
    }


    @PostMapping("/bookings/{id}/approve")
    public ResponseEntity<String> approveBooking(@PathVariable Long id) {
        try {
            adminService.approveBooking(id);
            return ResponseEntity.ok("Booking approved.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error approving booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to approve booking.");
        }
    }


    @PostMapping("/bookings/{id}/reject")
    public ResponseEntity<String> rejectBooking(@PathVariable Long id) {
        try {
            adminService.rejectBooking(id);
            return ResponseEntity.ok("Booking rejected.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error rejecting booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reject booking.");
        }
    }

    @PostMapping("/bookings/{id}/updateStatus/{status}")
    public ResponseEntity<BookingDTO> updateBookingStatus(@PathVariable Long id, @PathVariable String status){
        try {
            return ResponseEntity.ok(adminService.updateBookingStatus(id, status));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            System.err.println("Error updating booking status for " + id + " to " + status + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error fetching user by ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id){
        try{
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error deleting user by ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/users/save")
    public ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO dto){
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/users/update/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.updateUser(id, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            System.err.println("Error updating user " + id + ": " + e.getMessage());
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

    @PostMapping("/bookings/create-by-admin")
    public ResponseEntity<String> createBookingByAdmin(@RequestBody AdminBookingRequestDTO request) {
        try {
            bookingService.createBookingAsAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Booking created successfully by Admin.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during admin booking creation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during admin booking creation.");
        }
    }


    @GetMapping("/bookings/filter")
    public ResponseEntity<List<BookingDTO>> getFilteredBookings(
            @RequestParam(required = false) Long guestHouseId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getFilteredBookings(guestHouseId, roomType, checkInDate, checkOutDate, status));
    }


    @PutMapping("/bookings/{id}")
    public ResponseEntity<BookingDTO> updateBooking(@PathVariable Long id, @RequestBody BookingDTO bookingDTO) {
        try {
            if (bookingDTO.getId() != null && !bookingDTO.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            bookingDTO.setId(id);
            BookingDTO updatedBooking = adminService.updateBooking(id, bookingDTO);
            return ResponseEntity.ok(updatedBooking);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during booking update: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        try {
            adminService.deleteBooking(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deleting booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete booking.");
        }
    }


    @GetMapping("/guesthouses-for-filter")
    public ResponseEntity<List<GuestHouseDTO>> getAllGuestHousesForFilter() {
        return ResponseEntity.ok(guestHouseService.getAllGuestHouses());
    }


    @GetMapping("/rooms/types-for-filter")
    public ResponseEntity<List<String>> getAllRoomTypesForFilter() {
        return ResponseEntity.ok(roomService.getDistinctRoomTypes());
    }
}