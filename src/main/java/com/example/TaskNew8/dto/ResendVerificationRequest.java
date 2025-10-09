package com.example.TaskNew8.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}