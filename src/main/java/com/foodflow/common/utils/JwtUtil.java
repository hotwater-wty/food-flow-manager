package com.foodflow.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET_KEY = "food-flow-manager-jwt-secret-key-2026";
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
