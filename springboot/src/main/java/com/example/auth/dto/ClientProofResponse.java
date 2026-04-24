package com.example.auth.dto;

/**
 * DTO retourné après calcul de la preuve HMAC côté client simulé.
 *
 * @author Poun
 * @version 3.2
 */
public class ClientProofResponse {

    /**
     * Email.
     */
    private String email;

    /**
     * Nonce généré.
     */
    private String nonce;

    /**
     * Timestamp epoch secondes.
     */
    private long timestamp;

    /**
     * Message signé.
     */
    private String message;

    /**
     * HMAC calculé.
     */
    private String hmac;

    /**
     * Retourne l'email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Modifie l'email.
     *
     * @param email nouvel email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retourne le nonce.
     *
     * @return nonce
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Modifie le nonce.
     *
     * @param nonce nouveau nonce
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * Retourne le timestamp.
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Modifie le timestamp.
     *
     * @param timestamp nouveau timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Retourne le message signé.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Modifie le message signé.
     *
     * @param message nouveau message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Retourne le HMAC.
     *
     * @return hmac
     */
    public String getHmac() {
        return hmac;
    }

    /**
     * Modifie le HMAC.
     *
     * @param hmac nouveau hmac
     */
    public void setHmac(String hmac) {
        this.hmac = hmac;
    }
}