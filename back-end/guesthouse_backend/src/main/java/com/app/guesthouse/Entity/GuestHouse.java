package com.app.guesthouse.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Data
@Table(name = "guest_houses")
@NoArgsConstructor
@AllArgsConstructor
public class GuestHouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String location;

    @OneToMany(mappedBy = "guestHouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms;
}