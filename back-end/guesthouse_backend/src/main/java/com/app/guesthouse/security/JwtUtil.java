package com.app.guesthouse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final byte[] SECRET_KEY_BYTES = "my_secure_secret_key_1234567890123456".getBytes();
    // Convert the byte array secret into a java.security.Key object for JJWT
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_BYTES); // <--- MODIFIED LINE: Convert byte[] to Key

    private final long EXPIRATION = 1000 * 60 * 60;

    /**
     * Generates a JWT token including username, role, and userId.
     * This is the preferred method to use for user logins.
     * @param username The subject of the token (e.g., user's email).
     * @param role The user's role.
     * @param userId The ID of the user.
     * @return The generated JWT string.
     */
    public String generateToken(String username, String role, Long userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId) // <--- ADDED userId CLAIM HERE
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // Use SECRET_KEY directly
                .compact();
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * Extracts the userId from the JWT token.
     * @param token The JWT token.
     * @return The userId as a Long, or null if not found or token is invalid.
     */
    public Long extractUserId(String token) {
        try {
            return parseToken(token).get("userId", Long.class); // <--- ADDED extractUserId METHOD
        } catch (Exception e) {
            System.err.println("Error extracting userId from token: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }







//    public String generateToken(String username, String role) {
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("role", role)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//
//    public String extractUsername(String token) {
//        return parseToken(token).getSubject();
//    }
//
//    public String extractRole(String token) {
//        return parseToken(token).get("role", String.class);
//    }
//
//    public boolean isTokenValid(String token, String username) {
//        return extractUsername(token).equals(username) &&
//                !parseToken(token).getExpiration().before(new Date());
//    }
//
//    private Claims parseToken(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(SECRET_KEY)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
}