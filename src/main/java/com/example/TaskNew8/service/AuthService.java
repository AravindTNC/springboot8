package com.example.TaskNew8.service;

import com.example.TaskNew8.dto.AuthResponse;
import com.example.TaskNew8.dto.LoginRequest;
import com.example.TaskNew8.dto.RegisterRequest;
import com.example.TaskNew8.exception.EmailNotVerifiedException;
import com.example.TaskNew8.exception.UserAlreadyExistsException;
import com.example.TaskNew8.model.RefreshToken;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.model.Role; 
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; 
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final TwoFactorService twoFactorService;
    private final AccountLockoutService accountLockoutService;

    public AuthResponse register(RegisterRequest request) {
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        Role userRole = Role.USER;
        
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                userRole = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + request.getRole() + ". Must be USER or ADMIN");
            }
        }
    
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .emailVerified(false)
                .twoFactorEnabled(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();
        
        userRepository.save(user);
        emailVerificationService.sendVerificationEmail(user);

        return AuthResponse.builder()
                .message("Registration successful. Please check your email to verify your account. Role: " + userRole)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        
        try {
            User userFromDb = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
          
            accountLockoutService.checkAccountLock(userFromDb);
            
            log.info("Attempting login for user: {}", request.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            
            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Please verify your email.");
            }
            
      
            accountLockoutService.resetFailedAttempts(user.getEmail());
            
 
            if (user.isTwoFactorEnabled()) {
                log.info("2FA required for user: {}", user.getEmail());
                
                return AuthResponse.builder()
                        .requires2FA(true)
                        .message("2FA verification required. Enter your Google Authenticator code.")
                        .build();
            }
            
            String accessToken = jwtService.generateAccessToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .requires2FA(false)
                    .message("Login successful.")
                    .build();
                    
        } catch (LockedException e) {
            log.error("Account locked for user: {}", request.getEmail());
            throw new RuntimeException("Account is locked. Please try again later.");
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", request.getEmail());
           
            accountLockoutService.recordFailedLogin(request.getEmail());
            throw new RuntimeException("Invalid email or password");
        } catch (DisabledException e) {
            log.error("Account disabled for user: {}", request.getEmail());
            throw new EmailNotVerifiedException("Please verify your email.");
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getEmail());
            accountLockoutService.recordFailedLogin(request.getEmail());
            throw new RuntimeException("Authentication failed");
        }
    }

    public AuthResponse loginWith2FA(LoginRequest request, String code) {
        
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
       
            accountLockoutService.checkAccountLock(user);
            
          
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Please verify your email.");
            }
            
            if (!user.isTwoFactorEnabled()) {
                throw new RuntimeException("2FA not enabled");
            }
            
      
            boolean isValid = twoFactorService.verify2FACode(user, code);
            
            if (!isValid) {
                accountLockoutService.recordFailedLogin(request.getEmail());
                throw new RuntimeException("Invalid 2FA code");
            }
            
           
            accountLockoutService.resetFailedAttempts(user.getEmail());
            
            log.info("2FA login successful for user: {}", user.getEmail());
            
            String accessToken = jwtService.generateAccessToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .requires2FA(false)
                    .message("Login successful with 2FA.")
                    .build();
                    
        } catch (BadCredentialsException e) {
            accountLockoutService.recordFailedLogin(request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }
}
