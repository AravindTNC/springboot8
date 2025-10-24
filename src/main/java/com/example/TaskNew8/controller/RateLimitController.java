package com.example.TaskNew8.controller;

import com.example.TaskNew8.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rate-limit")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitService rateLimitService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getRateLimitInfo(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String key = "rate_limit:" + ipAddress;
        
        Map<String, Object> response = new HashMap<>();
        response.put("limit", 100);
        response.put("remaining", rateLimitService.getRemainingRequests(key));
        response.put("resetIn", rateLimitService.getSecondsUntilReset(key) + " seconds");
        
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}