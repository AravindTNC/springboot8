package com.example.TaskNew8.service;

import com.example.TaskNew8.exception.InvalidVerificationTokenException;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${application.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(User user) {
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken, baseUrl);
        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));

        if (user.isEmailVerified()) {
            return "Email is already verified. You can login now.";
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null); // Clear the token after verification
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
        return "Email verified successfully! You can now login.";
    }

    @Transactional
    public String resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        sendVerificationEmail(user);
        return "Verification email resent successfully. Please check your email.";
    }
}