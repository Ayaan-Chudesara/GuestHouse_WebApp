package com.app.guesthouse.Entity;

import jakarta.persistence.*;
import lombok.Data; // Consolidated to @Data which includes @Getter, @Setter
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data // Includes @Getter and @Setter, so removed explicit ones
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // A booking is made by one user
    @JoinColumn(name = "user_id", nullable = false) // foreign key column and not null
    private User user;  //references user entity

    @OneToOne(fetch = FetchType.LAZY) // <--- CONFIRMED: One booking is for one specific bed
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;  //references bed entity

    @Column(nullable = false)
    private LocalDate bookingDate;  //date of booking (check-in date)

    @Column(nullable = false)
    private Integer durationDays; // duration of stay in days

    private String purpose;

    @Enumerated(EnumType.STRING)  // to store enum as string
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        CHECKED_IN,
        CANCELLED, CHECKED_OUT
    }
}