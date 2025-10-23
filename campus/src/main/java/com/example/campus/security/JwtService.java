package com.example.campus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private Key signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(Map.of(
                        "uid", principal.getUserId(),
                        "role", principal.getRole().name()))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserPrincipal principal) {
        String username = extractUsername(token);
        return username.equals(principal.getUsername()) && !isExpired(token);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object id = extractAllClaims(token).get("uid");
        if (id instanceof Integer integer) {
            return integer.longValue();
        }
        if (id instanceof Long longValue) {
            return longValue;
        }
        if (id instanceof String string) {
            return Long.parseLong(string);
        }
        return null;
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    private boolean isExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        if (signingKey == null) {
            String secret = properties.getSecret();
            if (secret == null || secret.length() < 32) {
                throw new IllegalStateException("JWT密钥长度不足，请在配置文件中设置app.security.jwt.secret");
            }
            byte[] keyBytes;
            if (isBase64(secret)) {
                keyBytes = Decoders.BASE64.decode(secret);
            } else {
                keyBytes = secret.getBytes();
            }
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return signingKey;
    }

    private boolean isBase64(String value) {
        try {
            Decoders.BASE64.decode(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
