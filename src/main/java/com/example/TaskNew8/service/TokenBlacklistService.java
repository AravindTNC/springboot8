package com.example.TaskNew8.service;

import com.example.TaskNew8.model.BlacklistedToken;
import com.example.TaskNew8.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    
    @Transactional
    public void blacklistToken(String token, LocalDateTime expiresAt, String reason) {
        if (token == null || token.isEmpty()) {
            return;
        }

        
        if (blacklistedTokenRepository.existsByToken(token)) {
            log.info("Token already blacklisted");
            return;
        }

        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .token(token)
                .blacklistedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .reason(reason)
                .build();

        blacklistedTokenRepository.save(blacklistedToken);
        log.info("Token blacklisted. Reason: {}", reason);
    }

    
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return blacklistedTokenRepository.existsByToken(token);
    }

    
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired blacklisted tokens");
        int deletedCount = blacklistedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Deleted {} expired blacklisted tokens", deletedCount);
    }
}
