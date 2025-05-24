package com.app.guesthouse.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor; // Added for explicit constructors
import lombok.AllArgsConstructor; // Added for explicit constructors
import java.util.List;

@Entity
@Data
@Table(name = "rooms")
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String roomNo; // Aligned with your provided field name

    @NotBlank
    private String roomType;

    private Integer numberOfBeds; // Confirmed: Capacity of the room

    @ManyToOne(fetch = FetchType.LAZY) // A room belongs to one guesthouse
    @JoinColumn(name = "guest_house_id", nullable = false) // Foreign key column
    private GuestHouse guestHouse; // Reference to GuestHouse entity

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bed> beds; // A room can have multiple physical beds
}