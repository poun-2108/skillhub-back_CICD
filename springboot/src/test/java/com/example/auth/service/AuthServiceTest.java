package com.example.auth.service;

import com.example.auth.AuthApplication;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.ClientProofRequest;
import com.example.auth.dto.ClientProofResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.entity.User;
import com.example.auth.repository.AuthNonceRepository;
import com.example.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tests du service d'authentification TP5.
 *
 * Cas couverts :
 * - inscription
 * - login HMAC valide
 * - login HMAC invalide
 * - timestamp expiré
 * - timestamp futur
 * - nonce déjà utilisé
 * - utilisateur inconnu
 * - /me avec et sans token
 * - logout
 * - changement de mot de passe
 *
 * @author Poun
 * @version 5.0
 */
@SpringBootTest(classes = AuthApplication.class)
@ActiveProfiles("test")
public class AuthServiceTest {

    /**
     * Service principal.
     */
    @Autowired
    private AuthService authService;

    /**
     * Service de simulation client.
     */
    @Autowired
    private ClientProofService clientProofService;

    /**
     * Repository utilisateur.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Repository nonce.
     */
    @Autowired
    private AuthNonceRepository authNonceRepository;

    /**
     * Service de chiffrement des mots de passe.
     */
    @Autowired
    private PasswordCryptoService passwordCryptoService;

    /**
     * Nettoyage avant chaque test.
     */
    @BeforeEach
    void setUp() {
        authNonceRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Crée un utilisateur de test.
     */
    private void registerDefaultUser() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Poun");
        request.setEmail("poun@gmail.com");
        request.setPassword("Azerty1234!@");
        authService.register(request);
    }

    /**
     * Construit une preuve valide.
     *
     * @return preuve client valide
     */
    private ClientProofResponse buildValidProof() {
        ClientProofRequest request = new ClientProofRequest();
        request.setEmail("poun@gmail.com");
        request.setPassword("Azerty1234!@");
        return clientProofService.buildProof(request);
    }

    /**
     * Transforme une preuve client en LoginRequest.
     *
     * @param proof preuve client
     * @return requête login
     */
    private LoginRequest toLoginRequest(ClientProofResponse proof) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(proof.getEmail());
        loginRequest.setNonce(proof.getNonce());
        loginRequest.setTimestamp(proof.getTimestamp());
        loginRequest.setHmac(proof.getHmac());
        return loginRequest;
    }

    /**
     * Teste une inscription valide.
     */
    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Poun");
        request.setEmail("poun@gmail.com");
        request.setPassword("Azerty1234!@");

        Map<String, Object> response = authService.register(request);

        Assertions.assertEquals("Inscription réussie", response.get("message"));
        Assertions.assertTrue(userRepository.findByEmail("poun@gmail.com").isPresent());
    }

    /**
     * Teste une inscription avec email déjà utilisé.
     */
    @Test
    void testRegisterDuplicateEmail() {
        registerDefaultUser();

        RegisterRequest request = new RegisterRequest();
        request.setName("Autre");
        request.setEmail("poun@gmail.com");
        request.setPassword("Azerty1234!@");

        Map<String, Object> response = authService.register(request);

        Assertions.assertEquals("Email déjà utilisé", response.get("error"));
    }

    /**
     * Teste une inscription avec mot de passe faible.
     */
    @Test
    void testRegisterWeakPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Poun");
        request.setEmail("poun@gmail.com");
        request.setPassword("123");

        Map<String, Object> response = authService.register(request);

        Assertions.assertNotNull(response.get("error"));
    }

    /**
     * Teste le login avec HMAC valide.
     */
    @Test
    void testLoginOkWithValidHmac() {
        registerDefaultUser();
        ClientProofResponse proof = buildValidProof();

        Map<String, Object> response = authService.login(toLoginRequest(proof));

        Assertions.assertEquals("Connexion réussie", response.get("message"));
        Assertions.assertNotNull(response.get("accessToken"));
        Assertions.assertEquals("poun@gmail.com", response.get("email"));
        Assertions.assertNotNull(response.get("expiresAt"));
    }

    /**
     * Teste le login avec HMAC invalide.
     */
    @Test
    void testLoginKoInvalidHmac() {
        registerDefaultUser();
        ClientProofResponse proof = buildValidProof();

        LoginRequest loginRequest = toLoginRequest(proof);
        loginRequest.setHmac("hmac-faux");

        Map<String, Object> response = authService.login(loginRequest);

        Assertions.assertEquals("HMAC invalide", response.get("error"));
    }

    /**
     * Teste le login avec timestamp expiré.
     */
    @Test
    void testLoginKoExpiredTimestamp() {
        registerDefaultUser();
        ClientProofResponse proof = buildValidProof();

        LoginRequest loginRequest = toLoginRequest(proof);
        loginRequest.setTimestamp((System.currentTimeMillis() / 1000) - 1000);

        Map<String, Object> response = authService.login(loginRequest);

        Assertions.assertEquals("Requête expirée", response.get("error"));
    }

    /**
     * Teste le login avec timestamp futur.
     */
    @Test
    void testLoginKoFutureTimestamp() {
        registerDefaultUser();
        ClientProofResponse proof = buildValidProof();

        LoginRequest loginRequest = toLoginRequest(proof);
        loginRequest.setTimestamp((System.currentTimeMillis() / 1000) + 1000);

        Map<String, Object> response = authService.login(loginRequest);

        Assertions.assertEquals("Requête expirée", response.get("error"));
    }

    /**
     * Teste le login avec nonce déjà utilisé.
     */
    @Test
    void testLoginKoNonceAlreadyUsed() {
        registerDefaultUser();
        ClientProofResponse proof = buildValidProof();

        LoginRequest firstLogin = toLoginRequest(proof);
        Map<String, Object> firstResponse = authService.login(firstLogin);

        Assertions.assertEquals("Connexion réussie", firstResponse.get("message"));

        LoginRequest secondLogin = toLoginRequest(proof);
        Map<String, Object> secondResponse = authService.login(secondLogin);

        Assertions.assertEquals("Nonce déjà utilisé", secondResponse.get("error"));
    }

    /**
     * Teste le login avec utilisateur inconnu.
     */
    @Test
    void testLoginKoUnknownUser() {
        ClientProofRequest request = new ClientProofRequest();
        request.setEmail("inconnu@gmail.com");
        request.setPassword("Azerty1234!@");

        ClientProofResponse proof = clientProofService.buildProof(request);

        Map<String, Object> response = authService.login(toLoginRequest(proof));

        Assertions.assertEquals("Utilisateur introuvable", response.get("error"));
    }

    /**
     * Teste le login sans email.
     */
    @Test
    void testLoginKoWithoutEmail() {
        LoginRequest request = new LoginRequest();
        request.setNonce("nonce-test");
        request.setTimestamp(System.currentTimeMillis() / 1000);
        request.setHmac("abc");

        Map<String, Object> response = authService.login(request);

        Assertions.assertEquals("Email obligatoire", response.get("error"));
    }

    /**
     * Teste le login sans nonce.
     */
    @Test
    void testLoginKoWithoutNonce() {
        LoginRequest request = new LoginRequest();
        request.setEmail("poun@gmail.com");
        request.setTimestamp(System.currentTimeMillis() / 1000);
        request.setHmac("abc");

        Map<String, Object> response = authService.login(request);

        Assertions.assertEquals("Nonce obligatoire", response.get("error"));
    }

    /**
     * Teste le login sans timestamp.
     */
    @Test
    void testLoginKoWithoutTimestamp() {
        LoginRequest request = new LoginRequest();
        request.setEmail("poun@gmail.com");
        request.setNonce("nonce-test");
        request.setHmac("abc");

        Map<String, Object> response = authService.login(request);

        Assertions.assertEquals("Timestamp obligatoire", response.get("error"));
    }

    /**
     * Teste le login sans HMAC.
     */
    @Test
    void testLoginKoWithoutHmac() {
        LoginRequest request = new LoginRequest();
        request.setEmail("poun@gmail.com");
        request.setNonce("nonce-test");
        request.setTimestamp(System.currentTimeMillis() / 1000);

        Map<String, Object> response = authService.login(request);

        Assertions.assertEquals("HMAC obligatoire", response.get("error"));
    }

    /**
     * Teste /me avec token valide.
     */
    @Test
    void testGetMeOkWithToken() {
        registerDefaultUser();
        ClientProofResponse proof = buildValidProof();

        Map<String, Object> loginResponse = authService.login(toLoginRequest(proof));
        String token = (String) loginResponse.get("accessToken");

        Map<String, Object> meResponse = authService.getMe("Bearer " + token);

        Assertions.assertEquals("Poun", meResponse.get("name"));
        Assertions.assertEquals("poun@gmail.com", meResponse.get("email"));
        Assertions.assertNotNull(meResponse.get("tokenExpiresAt"));
    }

    /**
     * Teste /me sans token.
     */
    @Test
    void testGetMeKoWithoutToken() {
        Map<String, Object> response = authService.getMe(null);

        Assertions.assertEquals("Token manquant ou invalide", response.get("error"));
    }

    /**
     * Teste /me avec token invalide.
     */
    @Test
    void testGetMeKoUnknownToken() {
        Map<String, Object> response = authService.getMe("Bearer token-inconnu");

        Assertions.assertEquals("Utilisateur non trouvé pour ce token", response.get("error"));
    }

    /**
     * Teste /me avec token expiré.
     */
    @Test
    void testGetMeKoExpiredToken() {
        registerDefaultUser();

        User user = userRepository.findByEmail("poun@gmail.com").orElseThrow();
        user.setToken("token-expire");
        user.setTokenExpiresAt(LocalDateTime.now().minusMinutes(1));
        userRepository.save(user);

        Map<String, Object> response = authService.getMe("Bearer token-expire");

        Assertions.assertEquals("Token expiré ou invalide", response.get("error"));
    }

    /**
     * Teste logout avec token valide.
     */
    @Test
    void testLogoutOk() {
        registerDefaultUser();
        ClientProofResponse proof = buildValidProof();

        Map<String, Object> loginResponse = authService.login(toLoginRequest(proof));
        String token = (String) loginResponse.get("accessToken");

        Map<String, Object> logoutResponse = authService.logout("Bearer " + token);

        Assertions.assertEquals("Déconnexion réussie", logoutResponse.get("message"));

        User user = userRepository.findByEmail("poun@gmail.com").orElseThrow();
        Assertions.assertNull(user.getToken());
        Assertions.assertNull(user.getTokenExpiresAt());
    }

    /**
     * Teste logout sans token.
     */
    @Test
    void testLogoutKoWithoutToken() {
        Map<String, Object> response = authService.logout(null);

        Assertions.assertEquals("Token manquant ou invalide", response.get("error"));
    }

    /**
     * Teste logout avec token inconnu.
     */
    @Test
    void testLogoutKoUnknownToken() {
        Map<String, Object> response = authService.logout("Bearer token-inconnu");

        Assertions.assertEquals("Utilisateur non trouvé", response.get("error"));
    }

    /**
     * Teste un login avec utilisateur invalide.
     */
    @Test
    void testLoginWithInvalidUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("fake@gmail.com");
        request.setNonce("test-nonce");
        request.setTimestamp(System.currentTimeMillis() / 1000);
        request.setHmac("fake-hmac");

        Map<String, Object> response = authService.login(request);

        Assertions.assertTrue(response.containsKey("error"));
    }

    /**
     * Teste un login avec HMAC faux.
     */
    @Test
    void testLoginWrongPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail("test@gmail.com");
        request.setPassword("Azerty1234!");
        authService.register(request);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setNonce("test-nonce");
        loginRequest.setTimestamp(System.currentTimeMillis() / 1000);
        loginRequest.setHmac("fake-hmac");

        Map<String, Object> response = authService.login(loginRequest);

        Assertions.assertTrue(response.containsKey("error"));
    }

    /**
     * Teste le changement de mot de passe réussi.
     */
    @Test
    void shouldChangePasswordSuccessfully() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Poun");
        registerRequest.setEmail("poun.tp5@test.com");
        registerRequest.setPassword("Ancien123!");

        authService.register(registerRequest);

        User user = userRepository.findByEmail("poun.tp5@test.com").orElse(null);
        Assertions.assertNotNull(user);

        user.setToken("token-test-tp5");
        user.setTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("Ancien123!");
        changePasswordRequest.setNewPassword("Nouveau123!");

        Map<String, Object> response = authService.changePassword(
                "Bearer token-test-tp5",
                changePasswordRequest
        );

        Assertions.assertEquals("Mot de passe changé avec succès", response.get("message"));

        User updatedUser = userRepository.findByEmail("poun.tp5@test.com").orElse(null);
        Assertions.assertNotNull(updatedUser);

        String decryptedPassword = passwordCryptoService.decrypt(updatedUser.getPasswordEncrypted());
        Assertions.assertEquals("Nouveau123!", decryptedPassword);
    }

    /**
     * Teste le refus du changement si l'ancien mot de passe est faux.
     */
    @Test
    void shouldRefuseChangePasswordWhenOldPasswordIsWrong() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Poun");
        registerRequest.setEmail("poun.tp5.wrong@test.com");
        registerRequest.setPassword("Ancien123!");

        authService.register(registerRequest);

        User user = userRepository.findByEmail("poun.tp5.wrong@test.com").orElse(null);
        Assertions.assertNotNull(user);

        user.setToken("token-test-wrong");
        user.setTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("FauxAncien123!");
        changePasswordRequest.setNewPassword("Nouveau123!");

        Map<String, Object> response = authService.changePassword(
                "Bearer token-test-wrong",
                changePasswordRequest
        );

        Assertions.assertEquals("Ancien mot de passe incorrect", response.get("error"));
    }

    /**
     * Tests du contrôleur d'authentification.
     *
     * @author Poun
     * @version 5.0
     */
    @SpringBootTest(classes = AuthApplication.class)
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    static class AuthControllerTest {

        /**
         * URL de base des routes d'authentification.
         */
        private static final String AUTH_URL = "/api/auth";

        /**
         * Outil pour convertir objet Java en JSON.
         */
        @Autowired
        private ObjectMapper objectMapper;

        /**
         * Outil pour simuler les appels HTTP.
         */
        @Autowired
        private MockMvc mockMvc;

        /**
         * Service HMAC utilisé pour générer la signature dans les tests.
         */
        @Autowired
        private HmacService hmacService;

        /**
         * Crée une requête d'inscription.
         *
         * @param name nom
         * @param email email
         * @param password mot de passe
         * @return objet RegisterRequest
         */
        private RegisterRequest buildRegisterRequest(String name, String email, String password) {
            RegisterRequest request = new RegisterRequest();
            request.setName(name);
            request.setEmail(email);
            request.setPassword(password);
            return request;
        }

        /**
         * Crée une requête de connexion TP3.
         *
         * @param email email utilisateur
         * @param password mot de passe utilisateur
         * @return objet LoginRequest
         */
        private LoginRequest buildLoginRequest(String email, String password) {
            LoginRequest request = new LoginRequest();

            long timestamp = System.currentTimeMillis() / 1000;
            String nonce = "nonce-" + System.nanoTime();
            String message = email + ":" + nonce + ":" + timestamp;
            String hmac = hmacService.hmacSha256(password, message);

            request.setEmail(email);
            request.setNonce(nonce);
            request.setTimestamp(timestamp);
            request.setHmac(hmac);

            return request;
        }

        /**
         * Crée une requête de connexion avec HMAC invalide.
         *
         * @param email email utilisateur
         * @return objet LoginRequest invalide
         */
        private LoginRequest buildInvalidLoginRequest(String email) {
            LoginRequest request = new LoginRequest();

            long timestamp = System.currentTimeMillis() / 1000;
            String nonce = "nonce-" + System.nanoTime();

            request.setEmail(email);
            request.setNonce(nonce);
            request.setTimestamp(timestamp);
            request.setHmac("hmac-invalide");

            return request;
        }

        /**
         * Envoie une requête POST /register.
         *
         * @param request données d'inscription
         * @return résultat MVC
         * @throws Exception si erreur
         */
        private MvcResult postRegister(RegisterRequest request) throws Exception {
            return mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(AUTH_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andReturn();
        }

        /**
         * Envoie une requête POST /login.
         *
         * @param request données de connexion
         * @return résultat MVC
         * @throws Exception si erreur
         */
        private MvcResult postLogin(LoginRequest request) throws Exception {
            return mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(AUTH_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andReturn();
        }

        /**
         * Vérifie qu'une inscription valide fonctionne.
         *
         * @throws Exception si erreur
         */
        @Test
        void testRegisterSuccess() throws Exception {
            RegisterRequest request = buildRegisterRequest("Jean", "jean@gmail.com", "Azerty1234!@");

            MvcResult result = postRegister(request);

            Assertions.assertEquals(200, result.getResponse().getStatus());
        }

        /**
         * Vérifie qu'un email déjà utilisé est refusé.
         *
         * @throws Exception si erreur
         */
        @Test
        void testRegisterDuplicate() throws Exception {
            RegisterRequest request = buildRegisterRequest("Sara", "sara@gmail.com", "Azerty1234!@");

            postRegister(request);
            MvcResult result = postRegister(request);

            Assertions.assertEquals(400, result.getResponse().getStatus());
        }

        /**
         * Vérifie qu'un email vide est refusé.
         *
         * @throws Exception si erreur
         */
        @Test
        void testRegisterWithoutEmail() throws Exception {
            RegisterRequest request = buildRegisterRequest("Test", "", "Azerty1234!@");

            MvcResult result = postRegister(request);

            Assertions.assertEquals(400, result.getResponse().getStatus());
        }

        /**
         * Vérifie qu'une connexion valide fonctionne.
         *
         * @throws Exception si erreur
         */
        @Test
        void testLoginSuccess() throws Exception {
            RegisterRequest registerRequest = buildRegisterRequest("Marie", "marie@gmail.com", "Azerty1234!@");
            postRegister(registerRequest);

            LoginRequest loginRequest = buildLoginRequest("marie@gmail.com", "Azerty1234!@");
            MvcResult result = postLogin(loginRequest);

            Assertions.assertEquals(200, result.getResponse().getStatus());
            Assertions.assertTrue(result.getResponse().getContentAsString().contains("accessToken"));
        }

        /**
         * Vérifie qu'un utilisateur absent est refusé.
         *
         * @throws Exception si erreur
         */
        @Test
        void testLoginUserNotFound() throws Exception {
            LoginRequest loginRequest = buildLoginRequest("no@gmail.com", "Azerty1234!@");
            MvcResult result = postLogin(loginRequest);

            Assertions.assertEquals(400, result.getResponse().getStatus());
        }

        /**
         * Vérifie qu'un HMAC invalide est refusé.
         *
         * @throws Exception si erreur
         */
        @Test
        void testLoginInvalidHmac() throws Exception {
            RegisterRequest registerRequest = buildRegisterRequest("Paul", "paul@gmail.com", "Azerty1234!@");
            postRegister(registerRequest);

            LoginRequest loginRequest = buildInvalidLoginRequest("paul@gmail.com");
            MvcResult result = postLogin(loginRequest);

            Assertions.assertEquals(400, result.getResponse().getStatus());
        }

        /**
         * Vérifie qu'un accès à /me sans token est refusé.
         *
         * @throws Exception si erreur
         */
        @Test
        void testMeWithoutToken() throws Exception {
            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(AUTH_URL + "/me")
            ).andReturn();

            Assertions.assertEquals(401, result.getResponse().getStatus());
        }

        /**
         * Vérifie qu'un logout sans token est refusé.
         *
         * @throws Exception si erreur
         */
        @Test
        void testLogoutWithoutToken() throws Exception {
            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(AUTH_URL + "/logout")
            ).andReturn();

            Assertions.assertEquals(401, result.getResponse().getStatus());
        }

    }

}