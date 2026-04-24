package com.example.auth.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Tests unitaires du GlobalExceptionHandler.
 * Vérifie que les RuntimeException sont correctement transformées en réponse d'erreur.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ── handleRuntimeException ────────────────────────────────────────────────

    @Test
    void retourneMapAvecCleError() {
        RuntimeException ex = new RuntimeException("Erreur de test");
        Map<String, Object> result = handler.handleRuntimeException(ex);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.containsKey("error"));
    }

    @Test
    void retourneMessageDeLException() {
        RuntimeException ex = new RuntimeException("Message specifique");
        Map<String, Object> result = handler.handleRuntimeException(ex);

        Assertions.assertEquals("Message specifique", result.get("error"));
    }

    @Test
    void gereExceptionAvecMessageNull() {
        RuntimeException ex = new RuntimeException((String) null);
        Map<String, Object> result = handler.handleRuntimeException(ex);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.containsKey("error"));
        Assertions.assertNull(result.get("error"));
    }

    @Test
    void gereExceptionAvecMessageVide() {
        RuntimeException ex = new RuntimeException("");
        Map<String, Object> result = handler.handleRuntimeException(ex);

        Assertions.assertEquals("", result.get("error"));
    }

    @Test
    void retourneUniquementLaCleError() {
        RuntimeException ex = new RuntimeException("test");
        Map<String, Object> result = handler.handleRuntimeException(ex);

        Assertions.assertEquals(1, result.size());
    }

    @Test
    void gereIllegalArgumentException() {
        RuntimeException ex = new IllegalArgumentException("Argument invalide");
        Map<String, Object> result = handler.handleRuntimeException(ex);

        Assertions.assertEquals("Argument invalide", result.get("error"));
    }

    @Test
    void gereIllegalStateException() {
        RuntimeException ex = new IllegalStateException("Etat invalide");
        Map<String, Object> result = handler.handleRuntimeException(ex);

        Assertions.assertEquals("Etat invalide", result.get("error"));
    }
}
