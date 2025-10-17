package com.example.TaskNew8.service;

import com.example.TaskNew8.dto.OAuthUserResponse;
import com.example.TaskNew8.model.RefreshToken;
import com.example.TaskNew8.model.Role;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        log.info("OAuth2 login from provider: {}", provider);
        log.info("OAuth2User attributes: {}", oauth2User.getAttributes());

        processOAuthUser(oauth2User, provider);
        return oauth2User;
    }

    @Transactional
    public User processOAuthUser(OAuth2User oauth2User, String provider) {
        String email = oauth2User.getAttribute("email");

        if (email == null) {
            if ("github".equals(provider)) {
                email = oauth2User.getAttribute("login") + "@github.com";
            } else {
                throw new RuntimeException("Email not found in OAuth2 response");
            }
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = createNewOAuthUser(oauth2User, email, provider);
        } else {
            user = updateExistingOAuthUser(user, oauth2User, provider);
        }

        return user;
    }

    private User createNewOAuthUser(OAuth2User oauth2User, String email, String provider) {
        String name = oauth2User.getAttribute("name");
        String[] nameParts = (name != null) ? name.split(" ", 2) : new String[]{"", ""};

        User user = User.builder()
                .email(email)
                .firstName(nameParts.length > 0 ? nameParts[0] : "")
                .lastName(nameParts.length > 1 ? nameParts[1] : "")
                .password("") // OAuth users don't need password
                .role(Role.USER)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .build();

        user = userRepository.save(user);
        log.info("New OAuth user created: {} via {}", email, provider);
        return user;
    }

    private User updateExistingOAuthUser(User user, OAuth2User oauth2User, String provider) {
        String name = oauth2User.getAttribute("name");

        if (name != null && (user.getFirstName() == null || user.getFirstName().isEmpty())) {
            String[] nameParts = name.split(" ", 2);
            user.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
            user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
        }

        user = userRepository.save(user);
        log.info("OAuth user updated: {} via {}", user.getEmail(), provider);
        return user;
    }

    public OAuthUserResponse getOAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return OAuthUserResponse.builder()
                .email(user.getEmail())
                .name(user.getFirstName() + " " + user.getLastName())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}

