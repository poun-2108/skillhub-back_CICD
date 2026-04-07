package com.example.auth.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service utilitaire pour calculer des signatures HMAC SHA-256.
 *
 * @author Poun
 * @version 3.3
 */
@Service
public class HmacService {

    /**
     * Secret partagé utilisé pour les calculs HMAC.
     */
    private static final String SECRET = "secret";

    /**
     * Construit le message canonique à signer.
     *
     * Format :
     * email:nonce:timestamp
     *
     * @param email email utilisateur
     * @param nonce nonce aléatoire
     * @param timestamp timestamp epoch en secondes
     * @return message à signer
     */
    public String buildMessage(String email, String nonce, long timestamp) {
        return email + ":" + nonce + ":" + timestamp;
    }

    /**
     * Calcule un HMAC SHA-256 puis encode le résultat en Base64.
     *
     * @param secret secret partagé
     * @param data message à signer
     * @return signature HMAC
     */
    public String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Erreur pendant le calcul du HMAC", e);
        }
    }

    /**
     * Méthode simple utilisée par les tests.
     * Elle calcule le HMAC avec le secret par défaut.
     *
     * @param data message à signer
     * @return signature HMAC
     */
    public String generateHmac(String data) {
        return hmacSha256(SECRET, data);
    }
}