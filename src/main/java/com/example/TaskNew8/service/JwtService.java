package com.example.TaskNew8.service;


import com.example.TaskNew8.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    
    @Value("${application.security.jwt.access-token-expiration-ms}")
    private long jwtExpirationMs;

    public String generateAccessToken(User user) {
        return buildToken(new HashMap<>(), user, jwtExpirationMs);
    }

    private String buildToken(
            Map<String, Object> extraClaims, 
            User userDetails, 
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // Email is the subject
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, User userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 