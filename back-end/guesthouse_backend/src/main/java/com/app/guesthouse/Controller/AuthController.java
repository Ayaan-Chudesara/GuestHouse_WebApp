package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.LoginRequestDTO;
import com.app.guesthouse.DTO.RegisterRequestDTO;
import com.app.guesthouse.DTO.AuthResponseDTO; // <--- NEW/UPDATED DTO
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.UserService;
import com.app.guesthouse.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserRepo userRepo, UserService userService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            // Authenticate the user credentials
            authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // If authentication is successful, retrieve the User entity
            User user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("User not found for email: " + request.getEmail()));

            // Extract necessary information for JWT
            String email = user.getEmail();
            String role = user.getRole().name(); // Assuming getRole().name() gives the string representation of the role
            Long userId = user.getId(); // <--- Get the userId

            // Generate the token with email, role, and userId
            String token = jwtUtil.generateToken(email, role, userId); // <--- FIXED: Passing userId

            // Return the token, role, and userId in a structured DTO
            return ResponseEntity.ok(new AuthResponseDTO(token, role, userId)); // <--- Using AuthResponseDTO
        } catch (BadCredentialsException e) {
            // Log for debugging (optional)
            // System.err.println("Bad credentials for email: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        } catch (NoSuchElementException e) {
            // This case should ideally not be hit if authentication was successful,
            // but it acts as a safeguard.
            System.err.println("User not found in repository after authentication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during login.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequestDTO registerRequestDTO) {
        try {
            String message = userService.registerUser(registerRequestDTO);
            if (message.equals("User registered successfully.")) {
                return ResponseEntity.status(HttpStatus.CREATED).body(message); // Use HttpStatus.CREATED for successful registration
            } else {
                return ResponseEntity.badRequest().body(message);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during registration.");
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest() {
        return "You are an ADMIN!";
    }
}