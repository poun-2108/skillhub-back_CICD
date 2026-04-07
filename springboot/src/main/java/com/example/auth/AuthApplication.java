package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée principal de l'application Spring Boot.
 *
 * @author Poun
 * @version 1.0
 */
@SpringBootApplication
public class AuthApplication {

    /**
     * Méthode principale de lancement.
     *
     * @param args arguments de démarrage
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}