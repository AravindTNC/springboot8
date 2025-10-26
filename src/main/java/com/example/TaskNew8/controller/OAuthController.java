package com.example.TaskNew8.controller;

import com.example.TaskNew8.dto.OAuthUserResponse;
import com.example.TaskNew8.model.User;
import com.example.TaskNew8.service.OAuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthUserService oauthUserService;

 
    @GetMapping("/success")
    public ResponseEntity<OAuthUserResponse> oauthSuccess(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        
        if (oauth2User == null) {
            return ResponseEntity.badRequest().build();
        }

        String provider = "unknown";
        
      
        if (oauth2User.getAttributes().containsKey("sub")) {
            provider = "google";
        } else if (oauth2User.getAttributes().containsKey("login")) {
            provider = "github";
        }

        log.info("OAuth2 login success for provider: {}", provider);

        
        User user = oauthUserService.processOAuthUser(oauth2User, provider);

      
        OAuthUserResponse response = oauthUserService.getOAuthResponse(user);
        response.setProvider(provider);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/login/google")
    public ResponseEntity<Map<String, String>> loginGoogle() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Redirect to /oauth2/authorization/google");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login/github")
    public ResponseEntity<Map<String, String>> loginGithub() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Redirect to /oauth2/authorization/github");
        return ResponseEntity.ok(response);
    }
}
