package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Booking; // For Booking.Status enum
import com.app.guesthouse.Service.BookingService;
import com.app.guesthouse.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.IllegalStateException;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException; // Import for specific error handling

import java.lang.IllegalArgumentException; // For invalid input

@RestController
@RequestMapping("/api/bookings")
//@CrossOrigin(origins = "*") // Consider restricting origins in production
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    @Autowired
    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    // Endpoint for regular user-initiated booking (status defaults to PENDING in service)
    @PostMapping("/create") // Added "/create" to distinguish from admin create
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        try {
            BookingDTO created = bookingService.createBooking(bookingDTO);
            return new ResponseEntity<>(created, HttpStatus.CREATED); // 201 Created
        } catch (NoSuchElementException e) { // User or Bed not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException | IllegalStateException e) { // Invalid dates, bed unavailable, etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or return e.getMessage()
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all bookings
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        try {
            BookingDTO dto = bookingService.getBookingById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (NoSuchElementException e) { // Service throws if not found
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error fetching booking by ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete a booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) { // e.g., cannot delete active booking
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            System.err.println("Error deleting booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable Long userId) {
        // Assuming service handles user existence check if needed
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    // Admin: Approve a booking (If this is only for admin, consider moving to AdminController)
    // If it's a general endpoint that anyone with permission can call, keep it.
    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingDTO> approveBooking(@PathVariable Long id) {
        try {
            // Using the BookingService method that updates status with enum
            BookingDTO dto = bookingService.updateBookingStatus(id, Booking.Status.APPROVED);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) { // Invalid state for approval
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("Error approving booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Admin/User: Cancel a booking (mark as CANCELLED, not REJECTED for user initiated cancel)
    // If a user cancels, it's CANCELLED. If admin rejects, it's REJECTED.
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        try {
            // Using the BookingService method that updates status with enum
            BookingDTO dto = bookingService.updateBookingStatus(id, Booking.Status.CANCELLED); // Changed to CANCELLED
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) { // Invalid state for cancellation
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("Error canceling booking " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // CHECK-IN endpoint
    @PutMapping("/{bookingId}/check-in")
    public ResponseEntity<String> checkIn(@PathVariable Long bookingId) {
        try {
            // Call the correct method from BookingService
            bookingService.updateBookingStatus(bookingId, Booking.Status.CHECKED_IN);
            return ResponseEntity.ok("Guest checked in successfully.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during check-in for booking " + bookingId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to check in guest.");
        }
    }

    // CHECK-OUT endpoint
    @PutMapping("/{bookingId}/check-out")
    public ResponseEntity<String> checkOut(@PathVariable Long bookingId) {
        try {
            // Call the correct method from BookingService
            bookingService.updateBookingStatus(bookingId, Booking.Status.CHECKED_OUT);
            return ResponseEntity.ok("Guest checked out successfully.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during check-out for booking " + bookingId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to check out guest.");
        }
    }

    @GetMapping("/filter-by-date")
    public ResponseEntity<List<BookingDTO>> getBookingsBetweenDates(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                                    @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(bookingService.getBookingsBetweenDates(start, end));
    }
}