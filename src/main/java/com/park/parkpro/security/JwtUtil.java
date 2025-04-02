package com.park.parkpro.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret; // Must be at least 32 characters for HS512

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email) // Modern method for setting subject
                .claim("role", role) // Add role as a claim
                .issuedAt(new Date()) // Modern method for issuance time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Modern method for expiration
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512) // Secure key generation
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))) // Secure key for verification
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            System.out.println("Token validated successfully for: " + getEmailFromToken(token));
            return true;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }
}