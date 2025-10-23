package com.example.campus.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /**
     * Secret key used to sign JWT tokens.
     */
    private String secret;

    /**
     * Token expiration time in minutes.
     */
    private long expirationMinutes = 120L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
}
