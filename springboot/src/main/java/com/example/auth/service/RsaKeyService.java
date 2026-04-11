package com.example.auth.service;

import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.MessageDigest;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Service de gestion des cles RSA par utilisateur.
 *
 * Chaque utilisateur possede une paire de cles RSA :
 * - cle publique : partageable
 * - cle privee : chiffree et stockee en base
 *
 * Utilisation :
 * - Alice chiffre avec la cle publique de Bob
 * - Bob dechiffre avec sa cle privee
 * - Les messages sont haches avec SHA-256
 *
 * @author Nirina
 * @version 1.0
 */
@Service
public class RsaKeyService {

    /**
     * Taille de la cle RSA en bits.
     */
    private static final int KEY_SIZE = 2048;

    /**
     * Algorithme RSA.
     */
    private static final String RSA_ALGORITHM = "RSA";

    /**
     * Algorithme de hachage SHA-256.
     */
    private static final String SHA256_ALGORITHM = "SHA-256";

    /**
     * Genere une paire de cles RSA pour un utilisateur.
     *
     * @return paire de cles RSA
     */
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la generation des cles RSA", e);
        }
    }

    /**
     * Convertit une cle publique en chaine Base64.
     *
     * @param publicKey cle publique
     * @return cle publique en Base64
     */
    public String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Convertit une cle privee en chaine Base64.
     *
     * @param privateKey cle privee
     * @return cle privee en Base64
     */
    public String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Hache un message avec SHA-256.
     *
     * @param message message a hacher
     * @return hash SHA-256 en hexadecimal
     */
    public String hashSha256(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hashBytes = digest.digest(message.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hachage SHA-256", e);
        }
    }
}