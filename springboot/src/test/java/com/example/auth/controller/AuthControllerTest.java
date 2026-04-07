package com.example.auth.controller;

import com.example.auth.AuthApplication;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.HmacService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests du controller d'authentification.
 *
 * Ces tests vérifient surtout le contenu renvoyé par les endpoints.
 *
 * @author Poun
 * @version 3.2
 */
@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    /**
     * URL de base.
     */
    private static final String URL = "/api/auth";

    /**
     * Outil MockMvc.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Convertisseur JSON.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Service HMAC.
     */
    @Autowired
    private HmacService hmacService;

    /**
     * Construit une requête d'inscription.
     *
     * @param email email utilisateur
     * @return requête d'inscription
     */
    private RegisterRequest buildRegister(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail(email);
        request.setPassword("Azerty1234!@");
        return request;
    }

    /**
     * Construit une requête de connexion valide.
     *
     * @param email email utilisateur
     * @return requête login
     */
    private LoginRequest buildLogin(String email) {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = "nonce-" + System.nanoTime();
        String message = email + ":" + nonce + ":" + timestamp;

        String hmac = hmacService.hmacSha256("Azerty1234!@", message);

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setNonce(nonce);
        request.setTimestamp(timestamp);
        request.setHmac(hmac);

        return request;
    }

    /**
     * Teste une inscription valide.
     *
     * @throws Exception si erreur
     */
    @Test
    void testRegisterOK() throws Exception {
        RegisterRequest request = buildRegister("test1@gmail.com");

        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();

        String response = result.getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("Inscription"));
    }

    /**
     * Teste une inscription avec email déjà utilisé.
     *
     * @throws Exception si erreur
     */
    @Test
    void testRegisterDuplicate() throws Exception {
        RegisterRequest request = buildRegister("dup@gmail.com");

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();

        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();

        String response = result.getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("Email déjà utilisé")
                || response.contains("Email deja utilise"));
    }

    /**
     * Teste une connexion valide.
     *
     * @throws Exception si erreur
     */
    @Test
    void testLoginOK() throws Exception {
        String email = "login@gmail.com";

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegister(email)))
        ).andReturn();

        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLogin(email)))
        ).andReturn();

        String response = result.getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("Connexion réussie")
                || response.contains("Connexion reussie"));
        Assertions.assertTrue(response.contains("accessToken"));
    }

    /**
     * Teste une connexion avec HMAC invalide.
     *
     * @throws Exception si erreur
     */
    @Test
    void testLoginInvalidHmac() throws Exception {
        String email = "fail@gmail.com";

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegister(email)))
        ).andReturn();

        LoginRequest request = buildLogin(email);
        request.setHmac("fake");

        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();

        String response = result.getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("HMAC invalide"));
    }

    /**
     * Teste /me sans token.
     *
     * @throws Exception si erreur
     */
    @Test
    void testMeWithoutToken() throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(URL + "/me")
        ).andReturn();

        String response = result.getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("Token manquant")
                || response.contains("Token manquant ou invalide"));
    }

    /**
     * Teste logout sans token.
     *
     * @throws Exception si erreur
     */
    @Test
    void testLogoutWithoutToken() throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(URL + "/logout")
        ).andReturn();

        String response = result.getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("Token manquant")
                || response.contains("Token manquant ou invalide"));
    }
}