package com.example.auth.controller;

import com.example.auth.dto.ClientProofRequest;
import com.example.auth.dto.ClientProofResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.AuthService;
import com.example.auth.service.ClientProofService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.auth.dto.ChangePasswordRequest;

import java.util.Map;

/**
 * Controller REST pour l authentification.
 *
 * Endpoints :
 * - POST /register
 * - GET  /verify-email
 * - POST /client-proof
 * - POST /login
 * - GET  /me
 * - POST /logout
 * - PUT  /change-password
 *
 * @author Nirina
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Service principal d authentification.
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
     * @param clientProofService service client simule
     */
    public AuthController(AuthService authService, ClientProofService clientProofService) {
        this.authService = authService;
        this.clientProofService = clientProofService;
    }

    /**
     * Endpoint d inscription.
     *
     * @param request donnees d inscription
     * @return reponse simple
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Endpoint de verification email.
     * Appele via le lien envoye par email.
     *
     * @param token token de verification
     * @return message de succes ou erreur
     */
    @GetMapping("/verify-email")
    public Map<String, Object> verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }

    /**
     * Endpoint utilitaire pour simuler le calcul cote client.
     *
     * @param request email + password
     * @return preuve complete
     */
    @PostMapping("/client-proof")
    public ClientProofResponse buildClientProof(@RequestBody ClientProofRequest request) {
        return clientProofService.buildProof(request);
    }

    /**
     * Endpoint de connexion.
     *
     * @param request preuve de connexion HMAC
     * @return reponse avec token ou erreur
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Endpoint pour recuperer l utilisateur connecte.
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
     * Endpoint de deconnexion.
     *
     * @param authorizationHeader header Authorization
     * @return message de deconnexion
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return authService.logout(authorizationHeader);
    }

    /**
     * Change le mot de passe de l utilisateur connecte.
     *
     * @param authorizationHeader header Authorization avec Bearer token
     * @param request ancien et nouveau mot de passe
     * @return reponse JSON
     */
    @PutMapping("/change-password")
    public Map<String, Object> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(authorizationHeader, request);
    }
}