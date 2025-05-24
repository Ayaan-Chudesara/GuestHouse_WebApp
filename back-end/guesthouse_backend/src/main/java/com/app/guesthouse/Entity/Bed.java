package com.app.guesthouse.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor; // Added for explicit constructors
import lombok.AllArgsConstructor; // Added for explicit constructors

@Entity
@Data
@Table(name = "beds")
@NoArgsConstructor
@AllArgsConstructor
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String bedNo; // e.g., "A", "B", or "1" if it's the only bed in a single room

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // Status of this specific bed

    @ManyToOne(fetch = FetchType.LAZY) // A bed belongs to one room
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public enum Status {
        AVAILABLE,
        BOOKED
    }
}