package com.example.auth.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Tests simples de l'entité AuthNonce.
 *
 * @author Poun
 * @version 1.0
 */
public class AuthNonceTest {

    /**
     * Vérifie les getters et setters principaux de AuthNonce.
     */
    @Test
    void testAuthNonceGettersAndSetters() {
        AuthNonce authNonce = new AuthNonce();
        User user = new User();

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusMinutes(5);

        user.setId(1L);
        user.setName("Poun");
        user.setEmail("poun@gmail.com");

        authNonce.setId(10L);
        authNonce.setUser(user);
        authNonce.setNonce("nonce-test-123");
        authNonce.setCreatedAt(createdAt);
        authNonce.setExpiresAt(expiresAt);
        authNonce.setConsumed(true);

        Assertions.assertEquals(10L, authNonce.getId());
        Assertions.assertEquals(user, authNonce.getUser());
        Assertions.assertEquals("nonce-test-123", authNonce.getNonce());
        Assertions.assertEquals(createdAt, authNonce.getCreatedAt());
        Assertions.assertEquals(expiresAt, authNonce.getExpiresAt());
        Assertions.assertTrue(authNonce.isConsumed());
    }

    /**
     * Vérifie un second cas avec consumed à false.
     */
    @Test
    void testAuthNonceConsumedFalse() {
        AuthNonce authNonce = new AuthNonce();

        authNonce.setConsumed(false);

        Assertions.assertFalse(authNonce.isConsumed());
    }

    /**
     * Vérifie qu'on peut modifier les valeurs.
     */
    @Test
    void testAuthNonceUpdateValues() {
        AuthNonce authNonce = new AuthNonce();

        LocalDateTime firstCreatedAt = LocalDateTime.now();
        LocalDateTime secondCreatedAt = firstCreatedAt.plusMinutes(1);

        authNonce.setNonce("nonce-1");
        authNonce.setCreatedAt(firstCreatedAt);
        authNonce.setConsumed(false);

        Assertions.assertEquals("nonce-1", authNonce.getNonce());
        Assertions.assertEquals(firstCreatedAt, authNonce.getCreatedAt());
        Assertions.assertFalse(authNonce.isConsumed());

        authNonce.setNonce("nonce-2");
        authNonce.setCreatedAt(secondCreatedAt);
        authNonce.setConsumed(true);

        Assertions.assertEquals("nonce-2", authNonce.getNonce());
        Assertions.assertEquals(secondCreatedAt, authNonce.getCreatedAt());
        Assertions.assertTrue(authNonce.isConsumed());
    }
}