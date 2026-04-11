package com.example.auth.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests du validateur de mot de passe.
 * ✅ Adapté après correction : longueur minimale = 12 (était 8).
 *
 * @author Poun
 * @version 5.0
 */
public class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    /**
     * Vérifie qu'un bon mot de passe est accepté (12 caractères minimum).
     */
    @Test
    void testValidPassword() {
        Assertions.assertTrue(validator.isValid("Bonjour123!Aa"));
    }

    /**
     * Vérifie qu'un mot de passe de 11 caractères est refusé (< 12).
     * ✅ Fix : la version précédente testait "Bon12!" (6 chars) — ce test
     * est plus précis car il cible exactement la limite de 12 caractères.
     */
    @Test
    void testPasswordTooShort() {
        // 11 caractères : juste en dessous de la limite
        Assertions.assertFalse(validator.isValid("Bonjour12!A")); // 11 chars
    }

    /**
     * Vérifie qu'un mot de passe de 12 caractères exactement est accepté.
     */
    @Test
    void testPasswordExactlyMinLength() {
        // 12 caractères : exactement la limite
        Assertions.assertTrue(validator.isValid("Bonjour12!Ab")); // 12 chars
    }

    /**
     * Vérifie qu'un mot de passe sans majuscule est refusé.
     */
    @Test
    void testPasswordWithoutUppercase() {
        Assertions.assertFalse(validator.isValid("bonjour1234!a"));
    }

    /**
     * Vérifie qu'un mot de passe sans minuscule est refusé.
     */
    @Test
    void testPasswordWithoutLowercase() {
        Assertions.assertFalse(validator.isValid("BONJOUR1234!A"));
    }

    /**
     * Vérifie qu'un mot de passe sans chiffre est refusé.
     */
    @Test
    void testPasswordWithoutDigit() {
        Assertions.assertFalse(validator.isValid("BonjourTest!!A"));
    }

    /**
     * Vérifie qu'un mot de passe sans caractère spécial est refusé.
     */
    @Test
    void testPasswordWithoutSpecialCharacter() {
        Assertions.assertFalse(validator.isValid("Bonjour12345A"));
    }

    /**
     * Vérifie qu'un mot de passe null est refusé.
     */
    @Test
    void testNullPassword() {
        Assertions.assertFalse(validator.isValid(null));
    }

    /**
     * Vérifie que getRulesMessage retourne un message non vide.
     */
    @Test
    void testGetRulesMessage() {
        String message = validator.getRulesMessage();
        Assertions.assertNotNull(message);
        Assertions.assertFalse(message.isBlank());
        Assertions.assertTrue(message.contains("12"));
    }
}