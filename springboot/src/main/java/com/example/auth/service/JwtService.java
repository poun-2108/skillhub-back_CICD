package com.example.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Service de generation et validation des JWT.
 *
 * Le JWT contient :
 * - l email de l utilisateur (subject)
 * - la date d emission
 * - la date d expiration
 *
 * @author Nirina
 * @version 1.0
 */
@Service
public class JwtService {

    /**
     * Cle secrete lue depuis application.properties.
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Duree de validite du JWT en millisecondes.
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Genere un JWT pour un utilisateur.
     *
     * @param email email de l utilisateur
     * @return token JWT signe
     */
    public String generateToken(String email) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait l email depuis un JWT.
     *
     * @param token JWT
     * @return email de l utilisateur
     */
    public String extractEmail(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Verifie si un JWT est valide.
     *
     * @param token JWT a verifier
     * @return true si valide, false sinon
     */
    public boolean isTokenValid(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}