package com.example.TaskNew8.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Verify2FACodeRequest {
    
    @NotBlank(message = "Code is required")
    private String code;
}