package com.example.TaskNew8.repository;

import com.example.TaskNew8.model.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    
    // Check if token is blacklisted
    boolean existsByToken(String token);
    
    // Find token
    Optional<BlacklistedToken> findByToken(String token);
    
    // Delete expired tokens (cleanup)
    @Modifying
    @Transactional
    int deleteByExpiresAtBefore(LocalDateTime dateTime);
}