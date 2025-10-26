package com.example.TaskNew8.config;

import com.example.TaskNew8.model.User;
import com.example.TaskNew8.model.Role;
import com.example.TaskNew8.model.RefreshToken;
import com.example.TaskNew8.repository.UserRepository;
import com.example.TaskNew8.service.JwtService;
import com.example.TaskNew8.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 login success for user: {}", email);

        if (email == null) {
            log.error("No email received from OAuth provider");
            response.sendRedirect("/login.html?error=missing_email");
            return;
        }

     
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
            
            User newUser = User.builder()
                    .email(email)
                    .firstName(nameParts.length > 0 ? nameParts[0] : "")
                    .lastName(nameParts.length > 1 ? nameParts[1] : "")
                    .password("") 
                    .role(Role.USER)
                    .emailVerified(true)
                    .twoFactorEnabled(false)
                    .build();
            
            log.info("Creating new OAuth user: {}", email);
            return userRepository.save(newUser);
        });

   
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Tokens generated for OAuth user: {}", user.getEmail());

   
        String encodedAccess = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String encodedRefresh = URLEncoder.encode(refreshToken.getToken(), StandardCharsets.UTF_8);

        
        String redirectUrl = "/success.html?accessToken=" + encodedAccess + "&refreshToken=" + encodedRefresh;

        response.sendRedirect(redirectUrl);
    }
}
