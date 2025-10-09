package com.example.TaskNew8.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String profilePictureUrl;
    private boolean emailVerified;
}