package com.example.auth.controller;

import com.example.auth.dto.ClientProofRequest;
import com.example.auth.dto.ClientProofResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.AuthService;
import com.example.auth.service.ClientProofService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.auth.dto.ChangePasswordRequest;
import java.util.Map;

/**
 * Controller REST pour l'authentification.
 *
 * TP3 :
 * - préparation de la preuve HMAC côté client
 * - transition vers un login sans mot de passe transmis
 *
 * @author Poun
 * @version 3.2
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Service principal d'authentification.
     */
    private final AuthService authService;

    /**
     * Service de simulation du client HMAC.
     */
    private final ClientProofService clientProofService;

    /**
     * Constructeur.
     *
     * @param authService service auth
     * @param clientProofService service client simulé
     */
    public AuthController(AuthService authService, ClientProofService clientProofService) {
        this.authService = authService;
        this.clientProofService = clientProofService;
    }

    /**
     * Endpoint d'inscription.
     *
     * @param request données d'inscription
     * @return réponse simple
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Endpoint de connexion.
     *
     * Pour l'étape 3.2, on reçoit déjà la structure TP3 :
     * email + nonce + timestamp + hmac.
     * La vraie vérification côté serveur sera branchée en 3.3.
     *
     * @param request preuve de connexion
     * @return réponse temporaire
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Endpoint utilitaire pour simuler le calcul côté client.
     *
     * Cet endpoint est pédagogique pour Postman et les tests du TP.
     *
     * @param request email + password
     * @return preuve complète
     */
    @PostMapping("/client-proof")
    public ClientProofResponse buildClientProof(@RequestBody ClientProofRequest request) {
        return clientProofService.buildProof(request);
    }

    /**
     * Endpoint pour récupérer l'utilisateur connecté.
     *
     * @param authorizationHeader header Authorization
     * @return infos utilisateur
     */
    @GetMapping("/me")
    public Map<String, Object> me(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return authService.getMe(authorizationHeader);
    }

    /**
     * Endpoint de déconnexion.
     *
     * @param authorizationHeader header Authorization
     * @return message de déconnexion
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return authService.logout(authorizationHeader);
    }
    /**
     * Change le mot de passe de l'utilisateur connecté.
     *
     * @param authorizationHeader header Authorization avec Bearer token
     * @param request ancien et nouveau mot de passe
     * @return réponse JSON
     */
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(authorizationHeader, request);
    }
}