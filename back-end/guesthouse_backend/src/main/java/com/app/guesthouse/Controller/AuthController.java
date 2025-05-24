package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.LoginRequestDTO;
import com.app.guesthouse.DTO.RegisterRequestDTO;
import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.UserService;
import com.app.guesthouse.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // Import for login error
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException; // Import for user not found

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager; // Use final
    private final JwtUtil jwtUtil; // Use final
    private final UserRepo userRepo; // Use final
    private final UserService userService; // Use final

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
            authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            User user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("User not found for email: " + request.getEmail())); // Should not happen after successful auth but good for safety
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            return ResponseEntity.ok(Map.of("token", token, "role", user.getRole().name()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        } catch (NoSuchElementException e) { // For user not found in repo after auth (edge case)
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
            // This logic relies on the string message from service, which is less ideal
            // than throwing exceptions from service.
            if (message.equals("User registered successfully.")) {
                return ResponseEntity.ok(message);
            } else {
                return ResponseEntity.badRequest().body(message); // Bad request for validation issues
            }
        } catch (IllegalArgumentException e) { // Catch if UserService throws this instead of returning string
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