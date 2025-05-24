package com.app.guesthouse.DTO;

import com.app.guesthouse.Entity.User; // Import User entity to reference its Role enum
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private User.Role role; // Use User.Role enum directly
    // Do NOT include password or confirmPassword in a general UserDTO
    // Password should only be in a specific RegisterRequestDTO or LoginRequestDTO
}