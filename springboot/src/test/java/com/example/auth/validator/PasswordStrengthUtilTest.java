package com.example.auth.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests de PasswordStrengthUtil — couverture complète de toutes les branches.
 *
 * @author Poun
 * @version 5.0
 */
public class PasswordStrengthUtilTest {

    private final PasswordStrengthUtil util = new PasswordStrengthUtil();

    // ─── calculateStrength ────────────────────────────────────────────────────

    @Test
    void testCalculateStrengthNull() {
        Assertions.assertEquals(0, PasswordStrengthUtil.calculateStrength(null));
    }

    @Test
    void testCalculateStrengthBlank() {
        Assertions.assertEquals(0, PasswordStrengthUtil.calculateStrength("   "));
    }

    @Test
    void testCalculateStrengthWeak() {
        int score = PasswordStrengthUtil.calculateStrength("123");
        Assertions.assertTrue(score < 3);
    }

    @Test
    void testCalculateStrengthMedium() {
        int score = PasswordStrengthUtil.calculateStrength("Azerty123");
        Assertions.assertTrue(score >= 3);
    }

    @Test
    void testCalculateStrengthStrong() {
        int score = PasswordStrengthUtil.calculateStrength("Azerty123!@");
        Assertions.assertTrue(score >= 4);
    }

    // ─── evaluate ─────────────────────────────────────────────────────────────

    @Test
    void testEvaluateRed() {
        Assertions.assertEquals(PasswordStrengthUtil.RED, util.evaluate("123"));
    }

    /**
     * ✅ Couvre la branche ORANGE (score 3–4) jusqu'ici non testée.
     */
    @Test
    void testEvaluateOrange() {
        // "Azerty123" → majuscule + minuscule + chiffre + longueur ≥ 8 → score 4 → ORANGE
        Assertions.assertEquals(PasswordStrengthUtil.ORANGE, util.evaluate("Azerty123"));
    }

    @Test
    void testEvaluateGreen() {
        Assertions.assertEquals(PasswordStrengthUtil.GREEN, util.evaluate("Azerty123!@"));
    }

    // ─── getMessage ───────────────────────────────────────────────────────────

    /**
     * ✅ Couvre getMessage() — méthode jamais testée auparavant.
     */
    @Test
    void testGetMessageNullPassword() {
        String msg = util.getMessage(null, PasswordStrengthUtil.RED);
        Assertions.assertEquals("Saisissez un mot de passe", msg);
    }

    @Test
    void testGetMessageBlankPassword() {
        String msg = util.getMessage("", PasswordStrengthUtil.RED);
        Assertions.assertEquals("Saisissez un mot de passe", msg);
    }

    @Test
    void testGetMessageRed() {
        String msg = util.getMessage("abc", PasswordStrengthUtil.RED);
        Assertions.assertEquals("Mot de passe faible", msg);
    }

    @Test
    void testGetMessageOrange() {
        String msg = util.getMessage("Azerty123", PasswordStrengthUtil.ORANGE);
        Assertions.assertEquals("Mot de passe moyen", msg);
    }

    @Test
    void testGetMessageGreen() {
        String msg = util.getMessage("Azerty123!@", PasswordStrengthUtil.GREEN);
        Assertions.assertEquals("Mot de passe fort", msg);
    }

    // ─── isPolicyValid ────────────────────────────────────────────────────────

    @Test
    void testPolicyValidTrue() {
        Assertions.assertTrue(util.isPolicyValid("Azerty123!@"));
    }

    @Test
    void testPolicyValidFalse() {
        Assertions.assertFalse(util.isPolicyValid("abc"));
    }

    // ─── passwordsMatch ───────────────────────────────────────────────────────

    @Test
    void testPasswordsMatchTrue() {
        Assertions.assertTrue(util.passwordsMatch("Azerty123!@", "Azerty123!@"));
    }

    @Test
    void testPasswordsMatchFalse() {
        Assertions.assertFalse(util.passwordsMatch("Azerty123!@", "Azerty999!@"));
    }

    @Test
    void testPasswordsMatchNullFirst() {
        Assertions.assertFalse(util.passwordsMatch(null, "Azerty123!@"));
    }

    @Test
    void testPasswordsMatchNullSecond() {
        Assertions.assertFalse(util.passwordsMatch("Azerty123!@", null));
    }
}