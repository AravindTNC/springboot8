package com.example.TaskNew8.repository;

import com.example.TaskNew8.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    // NEW METHOD
    Optional<User> findByVerificationToken(String token);
}