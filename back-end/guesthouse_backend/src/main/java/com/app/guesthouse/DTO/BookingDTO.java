package com.app.guesthouse.DTO;

// Correct import for Booking.Status enum
import com.app.guesthouse.Entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {

    private Long id;

    // Direct fields from Booking entity
    private LocalDate bookingDate;
    private Integer durationDays;
    private String purpose;
    private Booking.Status status; // Use the correct enum
    private LocalDateTime createdAt;

    // Derived user details from Booking.user
    private Long userId; // Include user ID if needed for frontend actions
    private String guestName;
    private String guestEmail;

    // Derived bed details from Booking.bed
    private Long bedId;
    private String bedNo;
    // If you need bed status in the list, add:
    // private String bedStatus; // E.g., BOOKED/AVAILABLE from Bed.status

    // Derived room details from Booking.bed.room
    private Long roomId;
    private String roomNo;       // Aligned with Room entity's roomNo
    private String roomType;
    private Integer numberOfBeds; // From Room entity

    // Derived guesthouse details from Booking.bed.room.guestHouse
    private Long guestHouseId;
    private String guestHouseName;
    private String guestHouseLocation; // From GuestHouse entity's location

    // Convenience derived dates for frontend (same as before)
    private LocalDate checkInDate;  // Same as bookingDate
    private LocalDate checkOutDate; // bookingDate + durationDays
}