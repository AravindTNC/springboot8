package com.example.TaskNew8.repository;

import com.example.TaskNew8.model.RefreshToken;
import com.example.TaskNew8.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user")
    Optional<RefreshToken> findByUser(User user);

    @Modifying
    @Transactional
    void deleteByUser(User user);

    // New method for automatic cleanup
    @Modifying
    @Transactional
    int deleteByExpiryDateBefore(Instant expiryDate);
}