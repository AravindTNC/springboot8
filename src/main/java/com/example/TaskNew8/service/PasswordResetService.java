package com.example.TaskNew8.service;

import com.example.TaskNew8.dto.ForgotPasswordRequest;
import com.example.TaskNew8.dto.ResetPasswordRequest;
import com.example.TaskNew8.exception.InvalidPasswordResetTokenException;
import com.example.TaskNew8.model.PasswordResetToken;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.PasswordResetTokenRepository;
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.password-reset-token-expiration-ms}")
    private long tokenExpirationMs;

    @Value("${application.base-url}")
    private String baseUrl;

    @Transactional
    public String initiatePasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

       
        passwordResetTokenRepository.deleteByUser(user);

       
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(tokenExpirationMs))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        
        emailService.sendPasswordResetEmail(user.getEmail(), token, baseUrl);

        log.info("Password reset initiated for user: {}", user.getEmail());
        
        return "Password reset email sent successfully. Please check your email.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid password reset token"));

        
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidPasswordResetTokenException("Password reset token has expired");
        }

  
        if (resetToken.isUsed()) {
            throw new InvalidPasswordResetTokenException("Password reset token has already been used");
        }

     
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());

        return "Password has been reset successfully. You can now login with your new password.";
    }

  
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void deleteExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        int deletedCount = passwordResetTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("Deleted {} expired password reset tokens", deletedCount);
    }
}