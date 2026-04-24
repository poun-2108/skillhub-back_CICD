package com.example.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires du JwtService.
 * Vérifie la génération, l'extraction et la validation des JWT.
 */
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "test-secret-key-suffisamment-longue-pour-hmac-sha256-minimum-32-chars";
    private static final long EXPIRATION = 3_600_000L; // 1 heure en ms

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    void genereTokenNonNul() {
        String token = jwtService.generateToken("user@test.com");
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isBlank());
    }

    @Test
    void genereTokenDistinctPourEmailsDifferents() {
        String t1 = jwtService.generateToken("alice@test.com");
        String t2 = jwtService.generateToken("bob@test.com");
        Assertions.assertNotEquals(t1, t2);
    }

    // ── extractEmail ──────────────────────────────────────────────────────────

    @Test
    void extraitEmailDepuisToken() {
        String token = jwtService.generateToken("user@test.com");
        String email = jwtService.extractEmail(token);
        Assertions.assertEquals("user@test.com", email);
    }

    @Test
    void extraitEmailCorrectementApresGeneration() {
        String email = "alice@example.com";
        String token = jwtService.generateToken(email);
        Assertions.assertEquals(email, jwtService.extractEmail(token));
    }

    // ── isTokenValid ──────────────────────────────────────────────────────────

    @Test
    void tokenValideAvecBonneCle() {
        String token = jwtService.generateToken("user@test.com");
        Assertions.assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void tokenInvalideAvecJetonFalsifie() {
        Assertions.assertFalse(jwtService.isTokenValid("header.payload.signature-fausse"));
    }

    @Test
    void tokenInvalideAvecChainVide() {
        Assertions.assertFalse(jwtService.isTokenValid(""));
    }

    @Test
    void tokenExpiré() {
        // Token avec expiration immédiate (0 ms)
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(expiredService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(expiredService, "jwtExpiration", 0L);

        String token = expiredService.generateToken("user@test.com");
        Assertions.assertFalse(expiredService.isTokenValid(token));
    }
}
