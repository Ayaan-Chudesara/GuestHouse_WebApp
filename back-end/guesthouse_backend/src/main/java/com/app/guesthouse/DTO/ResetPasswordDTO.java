package com.app.guesthouse.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank
    private String token;
    
    @NotBlank
    private String newPassword;
} 