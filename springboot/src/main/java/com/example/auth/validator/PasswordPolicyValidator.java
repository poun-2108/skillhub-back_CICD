package com.example.auth.validator;

/**
 * Validateur simple de politique de mot de passe pour TP2.
 *
 * Règles imposées :
 * - au moins 12 caractères
 * - au moins une majuscule
 * - au moins une minuscule
 * - au moins un chiffre
 * - au moins un caractère spécial
 *
 * @author Poun
 * @version 2.2
 */
public class PasswordPolicyValidator {

    /**
     * Vérifie si le mot de passe respecte la politique demandée.
     *
     * @param password mot de passe à vérifier
     * @return true si le mot de passe est valide, sinon false
     */
    /**
     * Vérifie si le mot de passe respecte les règles de sécurité.
     *
     * @param password mot de passe
     * @return true si valide, sinon false
     */
    public boolean isValid(String password) {

        if (password == null) {
            return false;
        }

        // longueur minimale
        if (password.length() < 8) {
            return false;
        }

        // contient majuscule
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // contient minuscule
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // contient chiffre
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

        // contient caractère spécial
        if (!password.matches(".*[!@#$%^&*()].*")) {
            return false;
        }

        return true;
    }
    /**
     * Retourne un message simple expliquant la règle.
     *
     * @return message de validation
     */
    public String getRulesMessage() {
        return "Le mot de passe doit contenir au moins 12 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.";
    }
}