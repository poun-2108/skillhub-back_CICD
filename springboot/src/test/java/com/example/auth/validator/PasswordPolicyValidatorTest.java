package com.example.auth.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests du validateur de mot de passe.
 *
 * @author Poun
 * @version 2.2
 */
public class PasswordPolicyValidatorTest {

    /**
     * Vérifie qu'un bon mot de passe est accepté.
     */
    @Test
    void testValidPassword() {
        PasswordPolicyValidator validator = new PasswordPolicyValidator();
        Assertions.assertTrue(validator.isValid("Bonjour123!A"));
    }

    /**
     * Vérifie qu'un mot de passe trop court est refusé.
     */
    @Test
    void testPasswordTooShort() {
        PasswordPolicyValidator validator = new PasswordPolicyValidator();
        Assertions.assertFalse(validator.isValid("Bon12!"));
    }

    /**
     * Vérifie qu'un mot de passe sans majuscule est refusé.
     */
    @Test
    void testPasswordWithoutUppercase() {
        PasswordPolicyValidator validator = new PasswordPolicyValidator();
        Assertions.assertFalse(validator.isValid("bonjour123!"));
    }

    /**
     * Vérifie qu'un mot de passe sans minuscule est refusé.
     */
    @Test
    void testPasswordWithoutLowercase() {
        PasswordPolicyValidator validator = new PasswordPolicyValidator();
        Assertions.assertFalse(validator.isValid("BONJOUR123!"));
    }

    /**
     * Vérifie qu'un mot de passe sans chiffre est refusé.
     */
    @Test
    void testPasswordWithoutDigit() {
        PasswordPolicyValidator validator = new PasswordPolicyValidator();
        Assertions.assertFalse(validator.isValid("BonjourTest!"));
    }

    /**
     * Vérifie qu'un mot de passe sans caractère spécial est refusé.
     */
    @Test
    void testPasswordWithoutSpecialCharacter() {
        PasswordPolicyValidator validator = new PasswordPolicyValidator();
        Assertions.assertFalse(validator.isValid("Bonjour12345"));
    }

    /**
     * Vérifie qu'un mot de passe null est refusé.
     */
    @Test
    void testNullPassword() {
        PasswordPolicyValidator validator = new PasswordPolicyValidator();
        Assertions.assertFalse(validator.isValid(null));
    }
}