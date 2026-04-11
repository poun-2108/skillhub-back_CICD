package com.example.auth.dto;

/**
 * DTO pour la requête d'inscription.
 *
 * Il contient :
 * - le nom
 * - l email
 * - le mot de passe
 * - le role (apprenant ou formateur)
 *
 * @author Poun
 * @version 1.1
 */
public class RegisterRequest {

    /**
     * Nom de l utilisateur.
     */
    private String name;

    /**
     * Email de l utilisateur.
     */
    private String email;

    /**
     * Mot de passe de l utilisateur.
     */
    private String password;

    /**
     * Role de l utilisateur.
     * Valeurs attendues : apprenant ou formateur
     */
    private String role;

    /**
     * Retourne le nom.
     *
     * @return nom utilisateur
     */
    public String getName() {
        return name;
    }

    /**
     * Modifie le nom.
     *
     * @param name nouveau nom
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne l email.
     *
     * @return email utilisateur
     */
    public String getEmail() {
        return email;
    }

    /**
     * Modifie l email.
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

    /**
     * Retourne le role.
     *
     * @return role utilisateur
     */
    public String getRole() {
        return role;
    }

    /**
     * Modifie le role.
     *
     * @param role nouveau role
     */
    public void setRole(String role) {
        this.role = role;
    }
}