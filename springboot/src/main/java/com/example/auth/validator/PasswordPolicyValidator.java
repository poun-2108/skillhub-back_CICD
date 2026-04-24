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
 * @version 2.3
 */
public class PasswordPolicyValidator {

    /**
     * Longueur minimale du mot de passe.
     * ✅ Fix : la longueur minimale était 8 au lieu de 12 (incohérence avec getRulesMessage).
     */
    private static final int MIN_LENGTH = 12;

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

        // longueur minimale : 12 caractères (fix : était 8)
        if (password.length() < MIN_LENGTH) {
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