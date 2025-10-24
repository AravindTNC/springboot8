package com.example.TaskNew8.service;

import com.example.TaskNew8.exception.AccountLockedException;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockoutService {

    private final UserRepository userRepository;

    // Configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    // Record failed login attempt
    @Transactional
    public void recordFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            log.warn("Failed login attempt {} for user: {}", attempts, email);

            // Lock account if max attempts reached
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockoutTime(LocalDateTime.now());
                log.warn("Account locked for user: {} due to {} failed attempts", email, attempts);
            }

            userRepository.save(user);
        });
    }

    // Reset failed attempts on successful login
    @Transactional
    public void resetFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
                log.info("Reset failed login attempts for user: {}", email);
            }
        });
    }

    // Check if account is locked
    public void checkAccountLock(User user) {
        if (user.isAccountLocked()) {
            // Check if lockout period has expired
            if (user.getLockoutTime() != null) {
                LocalDateTime unlockTime = user.getLockoutTime().plusMinutes(LOCKOUT_DURATION_MINUTES);
                
                if (LocalDateTime.now().isAfter(unlockTime)) {
                    // Unlock account automatically
                    unlockAccount(user.getEmail());
                    log.info("Account automatically unlocked for user: {}", user.getEmail());
                } else {
                    // Still locked
                    long minutesRemaining = java.time.Duration.between(
                        LocalDateTime.now(), unlockTime
                    ).toMinutes();
                    
                    throw new AccountLockedException(
                        String.format("Account is locked due to too many failed login attempts. " +
                                     "Please try again in %d minutes.", minutesRemaining)
                    );
                }
            }
        }
    }

    // Unlock account manually (admin)
    @Transactional
    public void unlockAccount(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockoutTime(null);
            userRepository.save(user);
            log.info("Account unlocked for user: {}", email);
        });
    }

    // Get lockout status
    public String getLockoutStatus(String email) {
        return userRepository.findByEmail(email)
            .map(user -> {
                if (user.isAccountLocked()) {
                    if (user.getLockoutTime() != null) {
                        LocalDateTime unlockTime = user.getLockoutTime()
                            .plusMinutes(LOCKOUT_DURATION_MINUTES);
                        long minutesRemaining = java.time.Duration.between(
                            LocalDateTime.now(), unlockTime
                        ).toMinutes();
                        
                        if (minutesRemaining > 0) {
                            return String.format("Account locked. %d minutes remaining.", minutesRemaining);
                        } else {
                            return "Account locked but can be unlocked now.";
                        }
                    }
                    return "Account locked.";
                }
                
                int attemptsLeft = MAX_FAILED_ATTEMPTS - user.getFailedLoginAttempts();
                return String.format("Account active. %d login attempts remaining.", attemptsLeft);
            })
            .orElse("User not found");
    }
}