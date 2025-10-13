package com.example.TaskNew8.service;

import com.example.TaskNew8.dto.ChangePasswordRequest;
import com.example.TaskNew8.dto.UpdateProfileRequest;
import com.example.TaskNew8.dto.UserProfileResponse;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getUserProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getEmail());
        
        return getUserProfile(updatedUser);
    }

    @Transactional
    public UserProfileResponse uploadProfilePicture(User user, MultipartFile file) {
        
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (!cloudinaryService.isImageFile(file)) {
            throw new RuntimeException("File must be an image (JPEG, PNG, GIF, etc.)");
        }

        if (!cloudinaryService.isFileSizeValid(file, 5)) {
            throw new RuntimeException("File size must not exceed 5MB");
        }

        
        if (user.getCloudinaryPublicId() != null) {
            cloudinaryService.deleteImage(user.getCloudinaryPublicId());
        }

       
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
        
        String imageUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        
        user.setProfilePictureUrl(imageUrl);
        user.setCloudinaryPublicId(publicId);
        
        User updatedUser = userRepository.save(user);
        log.info("Profile picture uploaded for user: {}", user.getEmail());
        
        return getUserProfile(updatedUser);
    }

    @Transactional
    public UserProfileResponse deleteProfilePicture(User user) {
        if (user.getCloudinaryPublicId() == null) {
            throw new RuntimeException("No profile picture to delete");
        }

       
        cloudinaryService.deleteImage(user.getCloudinaryPublicId());

        
        user.setProfilePictureUrl(null);
        user.setCloudinaryPublicId(null);
        
        User updatedUser = userRepository.save(user);
        log.info("Profile picture deleted for user: {}", user.getEmail());
        
        return getUserProfile(updatedUser);
    }

    @Transactional
    public String changePassword(User user, ChangePasswordRequest request) {
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

      
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed for user: {}", user.getEmail());
        
        return "Password changed successfully";
    }
}