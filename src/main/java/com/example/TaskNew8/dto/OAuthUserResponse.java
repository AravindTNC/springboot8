package com.example.TaskNew8.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserResponse {
    private String email;
    private String name;
    private String provider;
    private String accessToken;
    private String refreshToken;
}