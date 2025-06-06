package com.app.guesthouse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${application.security.jwt.secret}")
    private String secretBase64EncodedString;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (secretBase64EncodedString == null || secretBase64EncodedString.isEmpty()) {
            throw new IllegalStateException("JWT secret key (application.security.jwt.secret) not loaded. Check application.properties.");
        }
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretBase64EncodedString);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            System.out.println("JwtUtil: Secret key initialized successfully by Base64 decoding.");
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: JWT secret key from properties is not a valid Base64 string!");
            throw new IllegalArgumentException("Invalid Base64 secret key in application.properties: " + e.getMessage(), e);
        }
    }

    public String generateToken(String username, String role, Long userId) {
        if (signingKey == null) {
            throw new IllegalStateException("JWT signing key has not been initialized. Check @PostConstruct in JwtUtil.");
        }
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        if (signingKey == null) {
            throw new IllegalStateException("JWT signing key has not been initialized for parsing. Check @PostConstruct in JwtUtil.");
        }
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }
}