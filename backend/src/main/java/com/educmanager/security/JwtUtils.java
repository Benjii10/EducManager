package com.educmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Création et lecture des tokens JWT (HMAC-SHA256).
 *
 * <p>[Benjamin – #01, 2026-10-15] Le secret et la durée de validité ne sont plus écrits en dur :
 * ils viennent de {@code application.properties} ({@code app.jwt.secret}, {@code app.jwt.expiration-ms}),
 * eux-mêmes surchargeables par les variables d'environnement {@code JWT_SECRET} / {@code JWT_EXPIRATION_MS}.</p>
 *
 * <p>[Benjamin – #05, 2026-10-15] Passage de jjwt 0.9.1 (2018, plus maintenu, nécessitait jaxb-api)
 * à jjwt 0.12 (API actuelle, clé typée {@link SecretKey}) — OWASP A06 « composants vulnérables ou obsolètes ».</p>
 */
@Component
public class JwtUtils {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtils(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        // HS256 exige une clé d'au moins 256 bits (32 octets)
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret doit faire au moins 32 caractères");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Génère un token pour un utilisateur
    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // Extrait le nom d'utilisateur du token
    public String getUsernameFromToken(String token) {
        return parse(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parse(token).get("role", String.class);
    }

    // Vérifie si le token est valide (signature + date d'expiration)
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
