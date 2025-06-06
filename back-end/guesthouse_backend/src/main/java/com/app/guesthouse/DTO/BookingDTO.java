package com.app.guesthouse.DTO;

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


    private LocalDate bookingDate;
    private Integer durationDays;
    private String purpose;
    private Booking.Status status;
    private LocalDateTime createdAt;
    private Integer numberOfGuests;


    private Long userId;
    private String guestName;
    private String guestEmail;

    private Long bedId;
    private String bedNo;

    private Long roomId;
    private String roomNo;
    private String roomType;
    private Integer numberOfBeds;

    private Long guestHouseId;
    private String guestHouseName;
    private String guestHouseLocation;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}