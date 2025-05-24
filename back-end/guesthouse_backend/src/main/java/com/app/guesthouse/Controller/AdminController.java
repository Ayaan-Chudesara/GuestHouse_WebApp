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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException; // Import this for specific error handling

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    // Use constructor injection for all dependencies
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
        // Services should throw exceptions, controllers catch them.
        // No need for a try-catch for generic Exception here unless you have specific recovery.
        // If `adminService.getAllBookings()` can throw specific exceptions, catch them.
        // For now, assuming it returns an empty list if no bookings, or throws a more specific error.
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @GetMapping("/allPendingBookings")
    public ResponseEntity<List<BookingDTO>> getAllPendingBookings() {
        return ResponseEntity.ok(adminService.getPendingBookings());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bookings/{id}/approve")
    public ResponseEntity<String> approveBooking(@PathVariable Long id) {
        try {
            adminService.approveBooking(id);
            return ResponseEntity.ok("Booking approved.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // e.g., if already approved or rejected
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error approving booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to approve booking.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bookings/{id}/reject")
    public ResponseEntity<String> rejectBooking(@PathVariable Long id) {
        try {
            adminService.rejectBooking(id);
            return ResponseEntity.ok("Booking rejected.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // e.g., if already approved or rejected
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
        } catch (IllegalArgumentException e) { // For invalid status string
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            System.err.println("Error updating booking status for " + id + " to " + status + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/all") // Changed path for clarity
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}") // Changed path for clarity
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

    // Assumed you meant @PathVariable for ID, if not, change back to @RequestParam
    @DeleteMapping("/users/{id}") // Changed path for clarity and @PathVariable
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id){
        try{
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error deleting user by ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/save") // Changed path for clarity
    public ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO dto){
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(dto)); // 201 Created
        } catch (IllegalArgumentException e) { // For email already exists or missing password
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or include error message
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/update/{id}") // Changed path for clarity
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.updateUser(id, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) { // For email already taken
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or include error message
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
        // Assuming service handles parsing of start/end strings to LocalDate if needed internally
        return ResponseEntity.ok(adminService.getSchedulerData(start, end));
    }

    @GetMapping("/dashboard/total-beds")
    public ResponseEntity<Integer> getTotalBeds() {
        return ResponseEntity.ok(adminService.getTotalBeds());
    }

    @PostMapping("/bookings/create-by-admin") // Clearer path for admin-created bookings
    public ResponseEntity<String> createBookingByAdmin(@RequestBody AdminBookingRequestDTO request) {
        try {
            bookingService.createBookingAsAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Booking created successfully by Admin."); // 201 Created
        } catch (NoSuchElementException e) { // e.g., Bed not found, User not found (if new user logic fails)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) { // e.g., Invalid dates, no beds found, bed not available
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during admin booking creation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during admin booking creation.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bookings/filter")
    public ResponseEntity<List<BookingDTO>> getFilteredBookings(
            @RequestParam(required = false) Long guestHouseId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getFilteredBookings(guestHouseId, roomType, checkInDate, checkOutDate, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/bookings/{id}")
    public ResponseEntity<BookingDTO> updateBooking(@PathVariable Long id, @RequestBody BookingDTO bookingDTO) {
        try {
            // It's good practice to ensure path ID matches body ID for PUT requests
            if (bookingDTO.getId() != null && !bookingDTO.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // ID mismatch
            }
            // Set the ID from the path to ensure consistency
            bookingDTO.setId(id);
            BookingDTO updatedBooking = adminService.updateBooking(id, bookingDTO);
            return ResponseEntity.ok(updatedBooking);
        } catch (NoSuchElementException e) { // Booking not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) { // Validation errors from service
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or return error details
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during booking update: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        try {
            adminService.deleteBooking(id);
            return ResponseEntity.noContent().build(); // 204 No Content for successful deletion
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deleting booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete booking.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/guesthouses-for-filter")
    public ResponseEntity<List<GuestHouseDTO>> getAllGuestHousesForFilter() {
        return ResponseEntity.ok(guestHouseService.getAllGuestHouses());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rooms/types-for-filter")
    public ResponseEntity<List<String>> getAllRoomTypesForFilter() {
        return ResponseEntity.ok(roomService.getDistinctRoomTypes());
    }
}