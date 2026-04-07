package com.example.auth.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests de PasswordStrengthUtil.
 *
 * @author Poun
 * @version 3.0
 */
public class PasswordStrengthUtilTest {

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

    @Test
    void testEvaluateRed() {
        PasswordStrengthUtil util = new PasswordStrengthUtil();
        Assertions.assertEquals(PasswordStrengthUtil.RED, util.evaluate("123"));
    }

    @Test
    void testEvaluateGreen() {
        PasswordStrengthUtil util = new PasswordStrengthUtil();
        Assertions.assertEquals(PasswordStrengthUtil.GREEN, util.evaluate("Azerty123!@"));
    }

    @Test
    void testPolicyValidTrue() {
        PasswordStrengthUtil util = new PasswordStrengthUtil();
        Assertions.assertTrue(util.isPolicyValid("Azerty123!@"));
    }

    @Test
    void testPolicyValidFalse() {
        PasswordStrengthUtil util = new PasswordStrengthUtil();
        Assertions.assertFalse(util.isPolicyValid("abc"));
    }

    @Test
    void testPasswordsMatchTrue() {
        PasswordStrengthUtil util = new PasswordStrengthUtil();
        Assertions.assertTrue(util.passwordsMatch("Azerty123!@", "Azerty123!@"));
    }

    @Test
    void testPasswordsMatchFalse() {
        PasswordStrengthUtil util = new PasswordStrengthUtil();
        Assertions.assertFalse(util.passwordsMatch("Azerty123!@", "Azerty999!@"));
    }
}