package com.example.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe de test pour vérifier que le contexte Spring Boot démarre correctement.
 *
 * @author Poun
 * @version 1.0
 */
@SpringBootTest(classes = AuthApplication.class)
@ActiveProfiles("test")
public class AuthApplicationTests {

    /**
     * Vérifie que le contexte Spring se charge sans erreur.
     */
    @Test
    void contextLoads() {
    }
}