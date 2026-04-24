package com.example.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Tests unitaires du RsaKeyService.
 * Vérifie la génération de paires RSA et le hachage SHA-256.
 */
class RsaKeyServiceTest {

    private RsaKeyService rsaKeyService;

    @BeforeEach
    void setUp() {
        rsaKeyService = new RsaKeyService();
    }

    // ── generateKeyPair ───────────────────────────────────────────────────────

    @Test
    void genereUnePaireDeClés() {
        KeyPair keyPair = rsaKeyService.generateKeyPair();
        Assertions.assertNotNull(keyPair);
        Assertions.assertNotNull(keyPair.getPublic());
        Assertions.assertNotNull(keyPair.getPrivate());
    }

    @Test
    void pairesDistinctesAChaqueFois() {
        KeyPair kp1 = rsaKeyService.generateKeyPair();
        KeyPair kp2 = rsaKeyService.generateKeyPair();
        // Les clés publiques encodées doivent être différentes
        Assertions.assertNotEquals(
                rsaKeyService.publicKeyToString(kp1.getPublic()),
                rsaKeyService.publicKeyToString(kp2.getPublic())
        );
    }

    // ── publicKeyToString ─────────────────────────────────────────────────────

    @Test
    void convertitClePubliqueEnBase64NonNul() {
        PublicKey pub = rsaKeyService.generateKeyPair().getPublic();
        String encoded = rsaKeyService.publicKeyToString(pub);
        Assertions.assertNotNull(encoded);
        Assertions.assertFalse(encoded.isBlank());
    }

    @Test
    void base64ClePubliqueEstDecodable() {
        PublicKey pub = rsaKeyService.generateKeyPair().getPublic();
        String encoded = rsaKeyService.publicKeyToString(pub);
        // Doit être du Base64 valide (pas d'exception)
        byte[] decoded = java.util.Base64.getDecoder().decode(encoded);
        Assertions.assertTrue(decoded.length > 0);
    }

    // ── privateKeyToString ────────────────────────────────────────────────────

    @Test
    void convertitClePriveeEnBase64NonNul() {
        PrivateKey priv = rsaKeyService.generateKeyPair().getPrivate();
        String encoded = rsaKeyService.privateKeyToString(priv);
        Assertions.assertNotNull(encoded);
        Assertions.assertFalse(encoded.isBlank());
    }

    @Test
    void base64ClePriveeEstDecodable() {
        PrivateKey priv = rsaKeyService.generateKeyPair().getPrivate();
        String encoded = rsaKeyService.privateKeyToString(priv);
        byte[] decoded = java.util.Base64.getDecoder().decode(encoded);
        Assertions.assertTrue(decoded.length > 0);
    }

    // ── hashSha256 ────────────────────────────────────────────────────────────

    @Test
    void hashSha256NonNul() {
        String hash = rsaKeyService.hashSha256("message de test");
        Assertions.assertNotNull(hash);
        Assertions.assertFalse(hash.isBlank());
    }

    @Test
    void hashSha256Longueur64() {
        // SHA-256 = 256 bits = 32 octets = 64 caractères hex
        String hash = rsaKeyService.hashSha256("test");
        Assertions.assertEquals(64, hash.length());
    }

    @Test
    void hashSha256Deterministe() {
        String h1 = rsaKeyService.hashSha256("meme-message");
        String h2 = rsaKeyService.hashSha256("meme-message");
        Assertions.assertEquals(h1, h2);
    }

    @Test
    void hashSha256DifferentPourMessagesDistincts() {
        String h1 = rsaKeyService.hashSha256("messageA");
        String h2 = rsaKeyService.hashSha256("messageB");
        Assertions.assertNotEquals(h1, h2);
    }

    @Test
    void hashSha256FormatHexValide() {
        String actual = rsaKeyService.hashSha256("abc");
        Assertions.assertTrue(actual.matches("[0-9a-f]{64}"),
                "Le hash doit être en hexadécimal lowercase sur 64 caractères");
    }
}
