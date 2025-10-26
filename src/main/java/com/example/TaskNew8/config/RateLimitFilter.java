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

    private static final int SC_TOO_MANY_REQUESTS = 429;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        
        String clientKey = getClientKey(request);

      
        if (!rateLimitService.isAllowed(clientKey)) {
         
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

    
        response.addHeader("X-RateLimit-Limit", "100");
        response.addHeader("X-RateLimit-Remaining",
                String.valueOf(rateLimitService.getRemainingRequests(clientKey)));

    
        filterChain.doFilter(request, response);
    }

  
    private String getClientKey(HttpServletRequest request) {
     
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
           
        }

     
        String ipAddress = getClientIpAddress(request);
        return "rate_limit:" + ipAddress;
    }

  
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
