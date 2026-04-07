package com.example.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controleur simple pour verifier que l'application fonctionne.
 */
@RestController
public class HomeController {

    /**
     * Route racine.
     * @return message simple
     */
    @GetMapping("/")
    public String home() {
        return "TP_5 Docker et GitHub Actions fonctionne";
    }
}