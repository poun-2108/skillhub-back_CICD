package com.example.auth.dto;

/**
 * DTO utilisé pour simuler le calcul côté client.
 *
 * Ce DTO sert uniquement à construire une preuve HMAC
 * à partir d'un email et d'un mot de passe saisi.
 *
 * @author Poun
 * @version 3.2
 */
public class ClientProofRequest {

    /**
     * Email de l'utilisateur.
     */
    private String email;

    /**
     * Mot de passe saisi côté client.
     */
    private String password;

    /**
     * Retourne l'email.
     *
     * @return email utilisateur
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
     * Retourne le mot de passe.
     *
     * @return mot de passe
     */
    public String getPassword() {
        return password;
    }

    /**
     * Modifie le mot de passe.
     *
     * @param password nouveau mot de passe
     */
    public void setPassword(String password) {
        this.password = password;
    }
}