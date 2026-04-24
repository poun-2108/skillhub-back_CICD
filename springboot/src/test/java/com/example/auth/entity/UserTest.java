package com.example.auth.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Tests de l'entité User (version adaptée à ton modèle réel).
 */
public class UserTest {

    /**
     * Vérifie les getters et setters principaux.
     */
    @Test
    void testUserBasicFields() {
        User user = new User();

        LocalDateTime now = LocalDateTime.now();

        user.setId(1L);
        user.setName("Poun");
        user.setEmail("poun@gmail.com");
        user.setToken("token-123");
        user.setTokenExpiresAt(now);
        user.setCreatedAt(now);

        Assertions.assertEquals(1L, user.getId());
        Assertions.assertEquals("Poun", user.getName());
        Assertions.assertEquals("poun@gmail.com", user.getEmail());
        Assertions.assertEquals("token-123", user.getToken());
        Assertions.assertEquals(now, user.getTokenExpiresAt());
        Assertions.assertEquals(now, user.getCreatedAt());
    }

    /**
     * Vérifie mise à jour du token.
     */
    @Test
    void testTokenUpdate() {
        User user = new User();

        user.setToken("old-token");
        Assertions.assertEquals("old-token", user.getToken());

        user.setToken("new-token");
        Assertions.assertEquals("new-token", user.getToken());
    }

    /**
     * Vérifie suppression du token (logout).
     */
    @Test
    void testTokenReset() {
        User user = new User();

        user.setToken("token-123");
        user.setTokenExpiresAt(LocalDateTime.now());

        user.setToken(null);
        user.setTokenExpiresAt(null);

        Assertions.assertNull(user.getToken());
        Assertions.assertNull(user.getTokenExpiresAt());
    }

    /**
     * Vérifie modification email.
     */
    @Test
    void testEmailUpdate() {
        User user = new User();

        user.setEmail("old@gmail.com");
        Assertions.assertEquals("old@gmail.com", user.getEmail());

        user.setEmail("new@gmail.com");
        Assertions.assertEquals("new@gmail.com", user.getEmail());
    }
}