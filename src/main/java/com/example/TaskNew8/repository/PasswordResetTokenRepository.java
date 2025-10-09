package com.example.TaskNew8.repository;

import com.example.TaskNew8.model.PasswordResetToken;
import com.example.TaskNew8.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
    
    @Modifying
    @Transactional
    int deleteByExpiryDateBefore(Instant expiryDate);
}
