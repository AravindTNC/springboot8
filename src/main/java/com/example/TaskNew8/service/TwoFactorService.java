package com.example.TaskNew8.service;

import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorService {

    private final UserRepository userRepository;

    @Value("${application.name:TaskNew8}")
    private String appName;

    private final CodeVerifier verifier = new DefaultCodeVerifier(
        new DefaultCodeGenerator(), 
        new SystemTimeProvider()
    );

    
    @Transactional
    public String setup2FA(User user) {
        if (user.isTwoFactorEnabled()) {
            throw new RuntimeException("2FA is already enabled");
        }

        // Generate secret
        String secret = new DefaultSecretGenerator().generate();
        
       
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        log.info("2FA setup initiated for user: {}", user.getEmail());
        
        return secret;
    }

   
    @Transactional
    public boolean verifyAndEnable2FA(User user, String code) {
        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("2FA secret not found");
        }

       
        boolean isValid = verifier.isValidCode(user.getTwoFactorSecret(), code);
        
        if (isValid) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            log.info("2FA enabled for user: {}", user.getEmail());
        } else {
            log.warn("Invalid 2FA code for user: {}", user.getEmail());
        }

        return isValid;
    }

    
    public boolean verify2FACode(User user, String code) {
        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            return false;
        }

        boolean isValid = verifier.isValidCode(user.getTwoFactorSecret(), code);
        
        if (!isValid) {
            log.warn("Invalid 2FA code during login for user: {}", user.getEmail());
        }

        return isValid;
    }

    
    @Transactional
    public void disable2FA(User user) {
        if (!user.isTwoFactorEnabled()) {
            throw new RuntimeException("2FA is not enabled");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        
        userRepository.save(user);
        log.info("2FA disabled for user: {}", user.getEmail());
    }
}