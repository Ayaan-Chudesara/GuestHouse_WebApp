package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.RegisterRequestDTO;
import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User mapToEntity(UserDTO dto) {
        User user = new User();
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRole(dto.getRole() != null ? dto.getRole() : User.Role.USER);
        return user;
    }

    public UserDTO mapToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
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
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
        return this.mapToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO saveUser(UserDTO dto) {
        if (dto.getId() == null && userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email '" + dto.getEmail() + "' already exists.");
        }
        if (dto.getId() == null && (dto.getPassword() == null || dto.getPassword().isBlank())) {
            throw new IllegalArgumentException("Password must be provided for new user registration.");
        }

        User user = this.mapToEntity(dto);
        User savedUser = userRepo.save(user);
        return this.mapToDTO(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
        userRepo.delete(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (userRepo.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already taken by another user.");
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        User updatedUser = userRepo.save(user);
        return this.mapToDTO(updatedUser);
    }



    @Override
    @Transactional
    public String registerUser(RegisterRequestDTO registerRequest) {
        if (registerRequest.getPassword() == null || registerRequest.getPassword().isBlank()) {
            return "Password cannot be empty.";
        }

        if (userRepo.existsByEmail(registerRequest.getEmail())) {
            return "Email already registered.";
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setRole(User.Role.USER);

        userRepo.save(user);
        return "User registered successfully.";
    }

    @Override
    public boolean verifyUserExists(Long userId) {
        return userRepo.existsById(userId);
    }
}