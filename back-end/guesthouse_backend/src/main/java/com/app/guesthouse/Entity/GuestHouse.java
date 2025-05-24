package com.app.guesthouse.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor; // Added for explicit constructors
import lombok.AllArgsConstructor; // Added for explicit constructors
import java.util.List; // To define one-to-many relationship

@Entity
@Data
@Table(name = "guest_houses") // Aligned with your table name
@NoArgsConstructor
@AllArgsConstructor
public class GuestHouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String location; // Aligned with your provided field name

    @OneToMany(mappedBy = "guestHouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms; // A guesthouse has many rooms
}