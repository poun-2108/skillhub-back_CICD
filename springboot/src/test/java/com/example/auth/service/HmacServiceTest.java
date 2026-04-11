package com.example.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests du service HMAC.
 *
 * @author Poun
 * @version 5.0
 */
public class HmacServiceTest {

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
     * Vérifie qu'un HMAC est calculé et non vide.
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

    /**
     * ✅ Couvre generateHmac() — méthode jusqu'ici non testée.
     * Vérifie que generateHmac retourne un résultat non nul.
     */
    @Test
    void testGenerateHmacNotNull() {
        String hmac = hmacService.generateHmac("message-de-test");
        Assertions.assertNotNull(hmac);
        Assertions.assertFalse(hmac.isBlank());
    }

    /**
     * Vérifie que generateHmac est déterministe pour le même message.
     */
    @Test
    void testGenerateHmacIsDeterministic() {
        String hmac1 = hmacService.generateHmac("message-test");
        String hmac2 = hmacService.generateHmac("message-test");
        Assertions.assertEquals(hmac1, hmac2);
    }

    /**
     * Vérifie que generateHmac produit des résultats différents pour des messages différents.
     */
    @Test
    void testGenerateHmacDifferentMessagesGiveDifferentOutputs() {
        String hmac1 = hmacService.generateHmac("messageA");
        String hmac2 = hmacService.generateHmac("messageB");
        Assertions.assertNotEquals(hmac1, hmac2);
    }
}