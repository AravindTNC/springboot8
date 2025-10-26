package com.example.TaskNew8.controller;

import com.example.TaskNew8.dto.*;
import com.example.TaskNew8.exception.TokenRefreshException;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;
    private final TokenBlacklistService tokenBlacklistService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(token -> {
                String newAccessToken = jwtService.generateAccessToken(token.getUser());
                return ResponseEntity.ok(AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(token.getToken()) 
                        .message("Token refresh successful.")
                        .build());
            })
            .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, 
                    "Refresh token is not in database!")); 
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
               
                LocalDateTime expiresAt = jwtService.getTokenExpiration(token);
                
             
                tokenBlacklistService.blacklistToken(token, expiresAt, "LOGOUT");
            }
            
           
            refreshTokenService.deleteByUserId(user.getId());
            
       
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok("Logout successful. Token invalidated.");
        }
        
        return ResponseEntity.badRequest().body("Invalid authentication.");
    }

   
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = passwordResetService.initiatePasswordReset(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(message);
    }

   
    
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        String message = emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        String message = emailVerificationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(message);
    }

   
    @PostMapping("/login/2fa")
    public ResponseEntity<AuthResponse> loginWith2FA(
            @Valid @RequestBody LoginRequest request,
            @RequestParam String code) {
        return ResponseEntity.ok(authService.loginWith2FA(request, code));
    }
}
