package com.app.guesthouse.DTO;

import ch.qos.logback.core.status.Status;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.User;
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
    private User user;
    private Bed bed;
    private LocalDate bookingDate;
    private Integer durationDays;
    private String purpose;
    private Booking.Status status;
    private LocalDateTime createdAt;

}
