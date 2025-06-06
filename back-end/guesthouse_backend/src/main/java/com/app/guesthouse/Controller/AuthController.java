package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.LoginRequestDTO;
import com.app.guesthouse.DTO.RegisterRequestDTO;
import com.app.guesthouse.DTO.AuthResponseDTO;
import com.app.guesthouse.DTO.ForgotPasswordDTO;
import com.app.guesthouse.DTO.ResetPasswordDTO;
import com.app.guesthouse.Entity.PasswordResetToken;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.PasswordResetTokenRepo;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.UserService;
import com.app.guesthouse.Service.Impl.MailServiceImpl;
import com.app.guesthouse.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;
    private final UserService userService;
    private final MailServiceImpl mailService;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserRepo userRepo, 
                        UserService userService, MailServiceImpl mailService, 
                        PasswordResetTokenRepo passwordResetTokenRepo, PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.userService = userService;
        this.mailService = mailService;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            User user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("User not found for email: " + request.getEmail()));

            String email = user.getEmail();
            String role = user.getRole().name();
            Long userId = user.getId();

            String token = jwtUtil.generateToken(email, role, userId);


            return ResponseEntity.ok(new AuthResponseDTO(token, role, userId));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        } catch (NoSuchElementException e) {
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
                return ResponseEntity.status(HttpStatus.CREATED).body(message);
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO request) {
        try {
            User user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            passwordResetTokenRepo.save(resetToken);

            mailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);

            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing password reset request");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO request) {
        try {
            PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(request.getToken())
                    .orElseThrow(() -> new NoSuchElementException("Invalid or expired token"));

            if (resetToken.isExpired()) {
                passwordResetTokenRepo.delete(resetToken);
                return ResponseEntity.badRequest().body("Token has expired");
            }

            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepo.save(user);

            passwordResetTokenRepo.delete(resetToken);

            return ResponseEntity.ok("Password has been reset successfully");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error resetting password");
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest() {
        return "You are an ADMIN!";
    }
}