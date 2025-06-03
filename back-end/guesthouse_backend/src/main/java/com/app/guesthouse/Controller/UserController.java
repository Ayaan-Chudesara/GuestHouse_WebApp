package com.app.guesthouse.Controller;

import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/verify")
    public ResponseEntity<Boolean> verifyUser(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(false);
            }
            System.out.println("Verifying user with ID: " + userId);
            boolean exists = userService.verifyUserExists(userId);
            System.out.println("User exists: " + exists);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid user ID format: " + e.getMessage());
            return ResponseEntity.badRequest().body(false);
        } catch (Exception e) {
            System.err.println("Error verifying user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/all")
    private ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    private ResponseEntity<UserDTO> getUserById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    private ResponseEntity deleteUserById(@RequestParam Long id){
        try{
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save")
    private ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.saveUser(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{id}")
    private ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto){
        try{
            return ResponseEntity.ok(userService.updateUser(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
