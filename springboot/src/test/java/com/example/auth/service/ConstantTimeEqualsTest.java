package com.example.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * Test de la comparaison en temps constant.
 *
 * @author Poun
 * @version 3.4
 */
public class ConstantTimeEqualsTest {

    /**
     * Teste égalité vraie.
     */
    @Test
    void testConstantTimeEqualsTrue() throws Exception {
        AuthService service = new AuthService(null, null, null, null);

        Method method = AuthService.class.getDeclaredMethod("constantTimeEquals", String.class, String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, "abc123", "abc123");

        Assertions.assertTrue(result);
    }

    /**
     * Teste égalité fausse.
     */
    @Test
    void testConstantTimeEqualsFalse() throws Exception {
        AuthService service = new AuthService(null, null, null, null);

        Method method = AuthService.class.getDeclaredMethod("constantTimeEquals", String.class, String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, "abc123", "abc124");

        Assertions.assertFalse(result);
    }

    /**
     * Teste longueur différente.
     */
    @Test
    void testConstantTimeEqualsDifferentLength() throws Exception {
        AuthService service = new AuthService(null, null, null, null);

        Method method = AuthService.class.getDeclaredMethod("constantTimeEquals", String.class, String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, "abc", "abcdef");

        Assertions.assertFalse(result);
    }
}