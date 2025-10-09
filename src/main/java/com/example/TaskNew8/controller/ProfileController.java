package com.example.TaskNew8.controller;

import com.example.TaskNew8.dto.ChangePasswordRequest;
import com.example.TaskNew8.dto.UpdateProfileRequest;
import com.example.TaskNew8.dto.UserProfileResponse;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Get current user profile
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        User user = getCurrentUser();
        return ResponseEntity.ok(profileService.getUserProfile(user));
    }

    // Update profile (first name, last name)
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser();
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }

    // Upload profile picture
    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        User user = getCurrentUser();
        return ResponseEntity.ok(profileService.uploadProfilePicture(user, file));
    }

    // Delete profile picture
    @DeleteMapping("/picture")
    public ResponseEntity<UserProfileResponse> deleteProfilePicture() {
        User user = getCurrentUser();
        return ResponseEntity.ok(profileService.deleteProfilePicture(user));
    }

    // Change password
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = getCurrentUser();
        String message = profileService.changePassword(user, request);
        return ResponseEntity.ok(message);
    }

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
}