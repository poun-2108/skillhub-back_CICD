package com.example.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests du service HMAC.
 *
 * @author Poun
 * @version 3.2
 */
public class HmacServiceTest {

    /**
     * Service à tester.
     */
    private final HmacService hmacService = new HmacService();

    /**
     * Vérifie que le message canonique est correct.
     */
    @Test
    void testBuildMessage() {
        String message = hmacService.buildMessage("poun@gmail.com", "abc-123", 1700000000L);

        Assertions.assertEquals("poun@gmail.com:abc-123:1700000000", message);
    }

    /**
     * Vérifie qu'un HMAC est calculé.
     */
    @Test
    void testHmacSha256NotNull() {
        String message = hmacService.buildMessage("poun@gmail.com", "abc-123", 1700000000L);
        String hmac = hmacService.hmacSha256("Azerty1234!@", message);

        Assertions.assertNotNull(hmac);
        Assertions.assertFalse(hmac.isBlank());
    }

    /**
     * Vérifie que deux calculs identiques donnent la même signature.
     */
    @Test
    void testHmacSha256SameInputSameOutput() {
        String message = hmacService.buildMessage("poun@gmail.com", "abc-123", 1700000000L);

        String hmac1 = hmacService.hmacSha256("Azerty1234!@", message);
        String hmac2 = hmacService.hmacSha256("Azerty1234!@", message);

        Assertions.assertEquals(hmac1, hmac2);
    }

    /**
     * Vérifie que deux secrets différents donnent des sorties différentes.
     */
    @Test
    void testHmacSha256DifferentSecretDifferentOutput() {
        String message = hmacService.buildMessage("poun@gmail.com", "abc-123", 1700000000L);

        String hmac1 = hmacService.hmacSha256("Azerty1234!@", message);
        String hmac2 = hmacService.hmacSha256("AutrePassword123!@", message);

        Assertions.assertNotEquals(hmac1, hmac2);
    }

}