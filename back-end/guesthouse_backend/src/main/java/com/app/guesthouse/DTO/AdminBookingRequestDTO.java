package com.app.guesthouse.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingRequestDTO {
    private String guestName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long guestHouseId;
    private String roomType;
    private int numberOfBeds;
    private int numberOfGuests;
    private String purpose;
}
