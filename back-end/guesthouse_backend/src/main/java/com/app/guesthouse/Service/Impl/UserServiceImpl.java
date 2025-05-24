package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.RegisterRequestDTO;
import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for transactional methods

import java.util.List;
import java.util.NoSuchElementException; // Added for proper error handling
import java.util.Optional; // Added for optional handling
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo; // Use final and constructor injection
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Maps a UserDTO to a User entity, encoding the password if present.
     * Used for creating or updating a User entity from DTO data.
     * @param dto The UserDTO containing user data.
     * @return The User entity.
     */
    public User mapToEntity(UserDTO dto) { // Renamed maptoEntity to mapToEntity for consistency
        User user = new User();
        // ID is usually only set for updates, not for creation.
        // If ID is provided in DTO for a new user, it might cause issues.
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        // Only encode password if it's provided in the DTO (for new user or password change)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        // Set role, defaulting to USER if not explicitly provided
        user.setRole(dto.getRole() != null ? dto.getRole() : User.Role.USER);
        return user;
    }

    /**
     * Maps a User entity to a UserDTO.
     * Note: Password is NOT mapped to the DTO for security reasons.
     * @param user The User entity.
     * @return The UserDTO without sensitive information like password.
     */
    public UserDTO mapToDTO(User user) { // Renamed maptoDTO to mapToDTO for consistency
        if (user == null) {
            return null; // Or throw an IllegalArgumentException
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        // Do NOT set password in DTO for security
        return dto;
    }



    @Override
    public List<UserDTO> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        // Use orElseThrow for proper error handling (e.g., HTTP 404)
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
        return this.mapToDTO(user);
    }

    @Override
    @Transactional // Ensures the operation is atomic
    public UserDTO saveUser(UserDTO dto) {
        // For new user registration, ensure email is not already used
        if (dto.getId() == null && userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email '" + dto.getEmail() + "' already exists.");
        }
        // Ensure password is provided for new user creation
        if (dto.getId() == null && (dto.getPassword() == null || dto.getPassword().isBlank())) {
            throw new IllegalArgumentException("Password must be provided for new user registration.");
        }

        User user = this.mapToEntity(dto); // Use the corrected mapToEntity
        User savedUser = userRepo.save(user);
        return this.mapToDTO(savedUser);
    }

    @Override
    @Transactional // Ensures the operation is atomic
    public void deleteUser(Long id) {
        // Check if the user exists before attempting to delete
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
        // IMPORTANT: Consider cascading deletes or orphaned data before deleting a user.
        // For example, what happens to their bookings?
        userRepo.delete(user);
    }

    @Override
    @Transactional // Ensures the operation is atomic
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));

        // Update fields individually, allowing partial updates
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }
        // Only update email if it's different and not already taken by another user
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (userRepo.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already taken by another user.");
            }
            user.setEmail(dto.getEmail());
        }
        // Only update password if a new one is provided in the DTO
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        User updatedUser = userRepo.save(user);
        return this.mapToDTO(updatedUser);
    }



    // This method is for specific registration logic, often used in auth flows
    @Override
    @Transactional // Ensures the operation is atomic
    public String registerUser(RegisterRequestDTO registerRequest) { // Changed parameter type
        // Password validation (can be handled by @NotBlank in DTO)
        if (registerRequest.getPassword() == null || registerRequest.getPassword().isBlank()) {
            return "Password cannot be empty.";
        }

        // Email existence check
        if (userRepo.existsByEmail(registerRequest.getEmail())) { // Use existsByEmail for cleaner check
            return "Email already registered.";
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setRole(User.Role.USER); // <--- Explicitly set default role here

        userRepo.save(user);
        return "User registered successfully.";
    }
}