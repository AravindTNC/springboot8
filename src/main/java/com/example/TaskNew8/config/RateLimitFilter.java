package com.example.TaskNew8.config;

import com.example.TaskNew8.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    // Define custom HTTP status code for clarity
    private static final int SC_TOO_MANY_REQUESTS = 429;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Identify client (IP address or user email if available)
        String clientKey = getClientKey(request);

        // Check if the client is allowed based on rate limits
        if (!rateLimitService.isAllowed(clientKey)) {
            // Rate limit exceeded
            response.setStatus(SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json");

            long secondsUntilReset = rateLimitService.getSecondsUntilReset(clientKey);

            String jsonResponse = String.format(
                "{\"error\": \"Too many requests\", " +
                "\"message\": \"Rate limit exceeded. Please try again in %d seconds.\", " +
                "\"retryAfter\": %d}",
                secondsUntilReset, secondsUntilReset
            );

            response.getWriter().write(jsonResponse);
            return;
        }

        // Add rate limit headers for client awareness
        response.addHeader("X-RateLimit-Limit", "100");
        response.addHeader("X-RateLimit-Remaining",
                String.valueOf(rateLimitService.getRemainingRequests(clientKey)));

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    // Determine client identifier
    private String getClientKey(HttpServletRequest request) {
        // Try extracting info from Authorization header (if applicable)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // In a real setup, parse token to extract user info (e.g., email)
            // For simplicity, we’ll stick with IP-based rate limiting
        }

        // Use IP address as fallback key
        String ipAddress = getClientIpAddress(request);
        return "rate_limit:" + ipAddress;
    }

    // Extract client IP address safely
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
