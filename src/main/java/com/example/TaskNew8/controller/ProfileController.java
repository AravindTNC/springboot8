package com.example.TaskNew8.controller;

import com.example.TaskNew8.dto.ChangePasswordRequest;
import com.example.TaskNew8.dto.UserProfileResponse;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;
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


    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        User user = getCurrentUser();
        return ResponseEntity.ok(profileService.uploadProfilePicture(user, file));
    }

    @PutMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateProfilePicture(
            @RequestParam("file") MultipartFile file) {
        User user = getCurrentUser();
        return ResponseEntity.ok(profileService.uploadProfilePicture(user, file));
    }

    @DeleteMapping("/picture")
    public ResponseEntity<UserProfileResponse> deleteProfilePicture() {
        User user = getCurrentUser();
        return ResponseEntity.ok(profileService.deleteProfilePicture(user));
    }

   
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
    // Update in ProfileController.java

@PutMapping("/password")
public ResponseEntity<String> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        HttpServletRequest httpRequest) {
    
    User user = getCurrentUser();
    
    // Get current token
    String authHeader = httpRequest.getHeader("Authorization");
    String token = null;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        token = authHeader.substring(7);
    }
    
    String message = profileService.changePassword(user, request, token);
    return ResponseEntity.ok(message);
}
}