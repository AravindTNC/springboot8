package com.example.TaskNew8.controller;

import com.example.TaskNew8.dto.UserResponse;
import com.example.TaskNew8.dto.UserUpdateRequest;
import com.example.TaskNew8.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    
    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Welcome! This endpoint is not secure.");
    }

    
    
    @GetMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getUserProfile(Principal principal) {
        UserResponse profile = userService.getUserProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> updateUserProfile(
            @RequestBody UserUpdateRequest updateRequest, 
            Principal principal) {
        
        boolean isAdmin = userService.isAdmin(principal.getName());
        UserResponse updatedProfile = userService.updateUserProfile(
            principal.getName(), 
            updateRequest, 
            principal.getName(), 
            isAdmin
        );
        return ResponseEntity.ok(updatedProfile);
    }
    
    @DeleteMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserProfile(Principal principal) {
        boolean isAdmin = userService.isAdmin(principal.getName());
        String result = userService.deleteUser(
            principal.getName(), 
            principal.getName(), 
            isAdmin
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/dashboard")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> userDashboard(Principal principal) {
        UserResponse profile = userService.getUserProfile(principal.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to User Dashboard, " + profile.getName() + "!");
        response.put("user", profile);
        
        return ResponseEntity.ok(response);
    }

   
    
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminDashboard(Principal principal) {
        UserResponse profile = userService.getUserProfile(principal.getName());
        List<UserResponse> allUsers = userService.getAllUsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Admin Dashboard, " + profile.getName() + "!");
        response.put("admin", profile);
        response.put("totalUsers", allUsers.size());
        response.put("users", allUsers);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
   
    @PutMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserByAdmin(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest updateRequest,
            Principal principal) {
        
        UserResponse updatedUser = userService.updateUserProfileById(
            userId, 
            updateRequest, 
            principal.getName()
        );
        return ResponseEntity.ok(updatedUser);
    }
    
   
    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserByAdmin(
            @PathVariable Long userId,
            Principal principal) {
        
        String result = userService.deleteUserById(userId, principal.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }
    
    
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Access Denied: " + e.getMessage());
        return ResponseEntity.status(403).body(response);
    }
    
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UsernameNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "User Not Found: " + e.getMessage());
        return ResponseEntity.status(404).body(response);
    }
}