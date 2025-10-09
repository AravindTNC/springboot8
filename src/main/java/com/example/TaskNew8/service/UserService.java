package com.example.TaskNew8.service;

import com.example.TaskNew8.dto.UserResponse;
import com.example.TaskNew8.dto.UserUpdateRequest;
import com.example.TaskNew8.exception.UserAlreadyExistsException;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    // Get user profile by email
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        return convertToResponse(user);
    }

    // Get user by ID
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        return convertToResponse(user);
    }

    // Get all users (Admin only)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Update user profile
    @Transactional
    public UserResponse updateUserProfile(String email, UserUpdateRequest request, 
                                         String currentUserEmail, boolean isAdmin) {
        // Check permission: user can only update their own profile unless they're admin
        if (!email.equals(currentUserEmail) && !isAdmin) {
            throw new AccessDeniedException("You don't have permission to update this user's profile");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated: {}", email);
        
        return convertToResponse(updatedUser);
    }

    // Delete user
    @Transactional
    public String deleteUser(String email, String currentUserEmail, boolean isAdmin) {
        // Check permission: user can only delete their own account unless they're admin
        if (!email.equals(currentUserEmail) && !isAdmin) {
            throw new AccessDeniedException("You don't have permission to delete this user");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        userRepository.delete(user);
        log.info("User deleted: {}", email);
        
        return "User deleted successfully: " + email;
    }

    // Check if user is admin
    public boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        return user.getRole().name().equals("ADMIN");
    }

    // Helper method to convert User to UserResponse
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getFirstName() + " " + user.getLastName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}