package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    // Create a new booking (status defaults to PENDING)
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        try {
            BookingDTO created = bookingService.createBooking(bookingDTO);
            if (created == null) {
                return ResponseEntity.badRequest().build(); // Invalid bed/user
            }
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all bookings
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        try {
            return ResponseEntity.ok(bookingService.getAllBookings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        try {
            BookingDTO dto = bookingService.getBookingById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete a booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Filter bookings by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    // ✅ Admin: Approve a booking
    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingDTO> approveBooking(@PathVariable Long id) {
        BookingDTO dto = bookingService.updateBookingStatus(id, Booking.Status.APPROVED);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    // ✅ Admin/User: Cancel a booking (mark as REJECTED)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        BookingDTO dto = bookingService.updateBookingStatus(id, Booking.Status.REJECTED);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{bookingId}/check-in")
    public ResponseEntity<String> checkIn(@PathVariable Long bookingId) {
        bookingService.updateStatus(bookingId, Booking.Status.CHECKED_IN);
        return ResponseEntity.ok("Guest checked in successfully.");
    }

    @PutMapping("/{bookingId}/check-out")
    public ResponseEntity<String> checkOut(@PathVariable Long bookingId) {
        bookingService.updateStatus(bookingId, Booking.Status.CHECKED_OUT);
        return ResponseEntity.ok("Guest checked out successfully.");
    }

    @GetMapping("/filter-by-date")
    public List<BookingDTO> getBookingsBetweenDates(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                    @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return bookingService.getBookingsBetweenDates(start, end);
    }


}
