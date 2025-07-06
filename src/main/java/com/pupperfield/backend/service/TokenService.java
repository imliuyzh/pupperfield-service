package com.pupperfield.backend.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

import static org.apache.commons.lang3.time.DateUtils.addHours;

/**
 * A service for generating and validating JSON Web Tokens (JWT).
 */
@AllArgsConstructor
@Service
public class TokenService {
    private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

    /**
     * Generates a token.
     *
     * @param email user's email
     * @param name user's name
     * @return a JWT string valid for one hour
     */
    public String generate(String email, String name) {
        return Jwts.builder()
            .header()
            .add("typ", "JWT")
            .and()
            .claims(Map.of("email", email, "name", name))
            .expiration(addHours(new Date(), 1))
            .issuedAt(new Date())
            .signWith(SECRET_KEY)
            .compact();
    }

    /**
     * Validates a token.
     *
     * @param token the JWT string
     * @return whether the token is valid
     */
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parse(token);
            return true;
        } catch (IllegalArgumentException | JwtException exception) {
            return false;
        }
    }
}
