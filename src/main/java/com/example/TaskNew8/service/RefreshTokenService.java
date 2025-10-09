package com.example.TaskNew8.service;

import com.example.TaskNew8.exception.TokenRefreshException;
import com.example.TaskNew8.model.RefreshToken;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.RefreshTokenRepository;
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token-expiration-ms}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository; 

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
    Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);

    if (existingToken.isPresent()) {
        RefreshToken token = existingToken.get();
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return refreshTokenRepository.save(token);
    } else {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(token); 
    }
}
    // public RefreshToken createRefreshToken(User user) {
    //     // Delete any existing refresh token for this user (since it's @OneToOne)
    //     refreshTokenRepository.deleteByUser(user);
        
    //     // Create new refresh token
    //     RefreshToken refreshToken = RefreshToken.builder()
    //             .user(user)
    //             .token(UUID.randomUUID().toString())
    //             .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
    //             .build();
        
    //     return refreshTokenRepository.save(refreshToken);
    // }



    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new sign-in request.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }

   
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void deleteExpiredTokens() {
        log.info("Starting automatic cleanup of expired refresh tokens");
        int deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("Deleted {} expired refresh tokens", deletedCount);
    }
}