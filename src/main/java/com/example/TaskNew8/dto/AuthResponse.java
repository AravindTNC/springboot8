
package com.example.TaskNew8.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    
  
    private String accessToken;
    private String refreshToken; 
    private String message;
}