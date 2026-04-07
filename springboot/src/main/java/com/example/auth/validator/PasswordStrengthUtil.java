package com.example.auth.validator;

/**
 * Utilitaire simple pour évaluer la robustesse d'un mot de passe.
 *
 * Cette classe est utilisée par l'interface JavaFX pour :
 * - évaluer la force du mot de passe
 * - afficher un message
 * - vérifier la politique minimale
 * - vérifier que deux mots de passe correspondent
 *
 * @author Poun
 * @version 3.0
 */
public class PasswordStrengthUtil {

    /**
     * Couleur rouge.
     */
    public static final String RED = "RED";

    /**
     * Couleur orange.
     */
    public static final String ORANGE = "ORANGE";

    /**
     * Couleur green.
     */
    public static final String GREEN = "GREEN";

    /**
     * Constructeur vide.
     */
    public PasswordStrengthUtil() {
        // constructeur vide volontaire
    }

    /**
     * Calcule un score de robustesse du mot de passe.
     *
     * @param password mot de passe
     * @return score entre 0 et 5
     */
    public static int calculateStrength(String password) {
        if (password == null || password.isBlank()) {
            return 0;
        }

        int score = 0;

        if (password.length() >= 8) {
            score++;
        }
        if (password.matches(".*[A-Z].*")) {
            score++;
        }
        if (password.matches(".*[a-z].*")) {
            score++;
        }
        if (password.matches(".*[0-9].*")) {
            score++;
        }
        if (password.matches(".*[!@#$%^&*()].*")) {
            score++;
        }

        return score;
    }

    /**
     * Évalue la couleur correspondant à la force du mot de passe.
     *
     * @param password mot de passe
     * @return RED, ORANGE ou GREEN
     */
    public String evaluate(String password) {
        int score = calculateStrength(password);

        if (score <= 2) {
            return RED;
        }
        if (score <= 4) {
            return ORANGE;
        }
        return GREEN;
    }

    /**
     * Retourne un message lisible selon la couleur.
     *
     * @param password mot de passe saisi
     * @param color couleur calculée
     * @return message à afficher
     */
    public String getMessage(String password, String color) {
        if (password == null || password.isBlank()) {
            return "Saisissez un mot de passe";
        }

        if (RED.equals(color)) {
            return "Mot de passe faible";
        }
        if (ORANGE.equals(color)) {
            return "Mot de passe moyen";
        }
        return "Mot de passe fort";
    }

    /**
     * Vérifie si le mot de passe respecte la politique minimale.
     *
     * @param password mot de passe
     * @return true si valide, sinon false
     */
    public boolean isPolicyValid(String password) {
        return calculateStrength(password) >= 4;
    }

    /**
     * Vérifie si deux mots de passe sont identiques.
     *
     * @param password mot de passe
     * @param confirmPassword confirmation
     * @return true si identiques, sinon false
     */
    public boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}