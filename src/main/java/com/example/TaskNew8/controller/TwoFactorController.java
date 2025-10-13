package com.example.TaskNew8.controller;

import com.example.TaskNew8.dto.Setup2FAResponse;
import com.example.TaskNew8.dto.TwoFactorStatusResponse;
import com.example.TaskNew8.dto.Verify2FACodeRequest;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.service.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    // Setup 2FA - Get secret
    @PostMapping("/setup")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Setup2FAResponse> setup2FA() {
        User user = getCurrentUser();
        
        String secret = twoFactorService.setup2FA(user);
        
        return ResponseEntity.ok(Setup2FAResponse.builder()
                .secret(secret)
                .message("Add this secret to Google Authenticator. Then verify the code.")
                .build());
    }

   
    @PostMapping("/enable")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> enable2FA(@Valid @RequestBody Verify2FACodeRequest request) {
        User user = getCurrentUser();
        
        boolean isValid = twoFactorService.verifyAndEnable2FA(user, request.getCode());
        
        Map<String, String> response = new HashMap<>();
        
        if (isValid) {
            response.put("message", "2FA enabled successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Invalid code");
            return ResponseEntity.badRequest().body(response);
        }
    }

  
    @PostMapping("/disable")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> disable2FA() {
        User user = getCurrentUser();
        
        twoFactorService.disable2FA(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA disabled successfully");
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TwoFactorStatusResponse> get2FAStatus() {
        User user = getCurrentUser();
        
        return ResponseEntity.ok(TwoFactorStatusResponse.builder()
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .email(user.getEmail())
                .build());
    }

    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
}