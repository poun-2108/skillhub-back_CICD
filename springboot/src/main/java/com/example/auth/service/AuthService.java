package com.example.auth.service;

import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.entity.AuthNonce;
import com.example.auth.entity.User;
import com.example.auth.repository.AuthNonceRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.validator.PasswordPolicyValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service contenant la logique metier de l authentification.
 *
 * Moustass CloudSec :
 * - JWT remplace l ancien token UUID
 * - confirmation email obligatoire
 * - paire de cles RSA generee a l inscription
 * - log des transactions
 * - changement de mot de passe pour un utilisateur authentifie
 *
 * SkillHub :
 * - gestion du role apprenant / formateur
 *
 * @author Nirina
 * @version 1.1
 */
@Service
public class AuthService {

    /**
     * Cle standard pour les erreurs dans les reponses.
     */
    private static final String KEY_ERROR = "error";

    /**
     * Cle standard pour les messages dans les reponses.
     */
    private static final String KEY_MESSAGE = "message";

    /**
     * Prefixe Bearer pour les headers Authorization.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Message d erreur token invalide.
     */
    private static final String MSG_TOKEN_INVALIDE = "Token manquant ou invalide";

    /**
     * Statut succes pour les logs.
     */
    private static final String LOG_SUCCESS = "SUCCESS";

    /**
     * Statut echec pour les logs.
     */
    private static final String LOG_FAILURE = "FAILURE";

    /**
     * Duree du JWT en millisecondes lue depuis application.properties.
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Repository utilisateur.
     */
    private final UserRepository userRepository;

    /**
     * Service de chiffrement reversible.
     */
    private final PasswordCryptoService passwordCryptoService;

    /**
     * Repository des nonces.
     */
    private final AuthNonceRepository authNonceRepository;

    /**
     * Service HMAC.
     */
    private final HmacService hmacService;

    /**
     * Service JWT.
     */
    private final JwtService jwtService;

    /**
     * Service email.
     */
    private final EmailService emailService;

    /**
     * Service RSA.
     */
    private final RsaKeyService rsaKeyService;

    /**
     * Service de log des transactions.
     */
    private final TransactionLogService transactionLogService;

    /**
     * Validateur de mot de passe.
     */
    private final PasswordPolicyValidator passwordPolicyValidator = new PasswordPolicyValidator();

    /**
     * Constructeur du service.
     *
     * @param userRepository repository utilisateur
     * @param passwordCryptoService service de chiffrement
     * @param authNonceRepository repository des nonces
     * @param hmacService service HMAC
     * @param jwtService service JWT
     * @param emailService service email
     * @param rsaKeyService service RSA
     * @param transactionLogService service de log
     */
    public AuthService(UserRepository userRepository,
                       PasswordCryptoService passwordCryptoService,
                       AuthNonceRepository authNonceRepository,
                       HmacService hmacService,
                       JwtService jwtService,
                       EmailService emailService,
                       RsaKeyService rsaKeyService,
                       TransactionLogService transactionLogService) {
        this.userRepository = userRepository;
        this.passwordCryptoService = passwordCryptoService;
        this.authNonceRepository = authNonceRepository;
        this.hmacService = hmacService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.rsaKeyService = rsaKeyService;
        this.transactionLogService = transactionLogService;
    }

    /**
     * Inscription d un utilisateur.
     * Genere une paire de cles RSA et envoie un email de confirmation.
     *
     * @param request donnees d inscription
     * @return reponse simple
     */
    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request.getName() == null || request.getName().isBlank()) {
            response.put(KEY_ERROR, "Nom obligatoire");
            return response;
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            response.put(KEY_ERROR, "Email obligatoire");
            return response;
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            response.put(KEY_ERROR, "Role obligatoire");
            return response;
        }

        if (!request.getRole().equals("apprenant") && !request.getRole().equals("formateur")) {
            response.put(KEY_ERROR, "Le role doit etre apprenant ou formateur");
            return response;
        }

        if (request.getPassword() == null || !passwordPolicyValidator.isValid(request.getPassword())) {
            transactionLogService.log("REGISTER", request.getEmail(), LOG_FAILURE, "Mot de passe invalide");
            response.put(KEY_ERROR, passwordPolicyValidator.getRulesMessage());
            return response;
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            transactionLogService.log("REGISTER", request.getEmail(), LOG_FAILURE, "Email deja utilise");
            response.put(KEY_ERROR, "Email déjà utilisé");
            return response;
        }

        String verificationToken = UUID.randomUUID().toString();
        KeyPair keyPair = rsaKeyService.generateKeyPair();

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPasswordEncrypted(passwordCryptoService.encrypt(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setToken(null);
        user.setTokenExpiresAt(null);
        user.setEmailVerified(false);
        user.setEmailVerificationToken(verificationToken);
        user.setPublicKey(rsaKeyService.publicKeyToString(keyPair.getPublic()));
        user.setPrivateKey(rsaKeyService.privateKeyToString(keyPair.getPrivate()));

        userRepository.save(user);

        emailService.sendVerificationEmail(request.getEmail(), verificationToken);

        transactionLogService.log("REGISTER", request.getEmail(), LOG_SUCCESS, "Inscription reussie");

        response.put(KEY_MESSAGE, "Inscription réussie. Veuillez confirmer votre email.");
        response.put("emailVerificationToken", verificationToken);
        response.put("role", user.getRole());

        return response;
    }

    /**
     * Verifie l email d un utilisateur via le token de verification.
     *
     * @param token token de verification
     * @return message de succes ou erreur
     */
    public Map<String, Object> verifyEmail(String token) {
        Map<String, Object> response = new HashMap<>();

        if (token == null || token.isBlank()) {
            response.put(KEY_ERROR, "Token de verification manquant");
            return response;
        }

        User user = userRepository.findByEmailVerificationToken(token).orElse(null);

        if (user == null) {
            transactionLogService.log("VERIFY_EMAIL", "inconnu", LOG_FAILURE, "Token invalide");
            response.put(KEY_ERROR, "Token de verification invalide");
            return response;
        }

        if (user.isEmailVerified()) {
            response.put(KEY_MESSAGE, "Email déjà confirmé");
            return response;
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        transactionLogService.log("VERIFY_EMAIL", user.getEmail(), LOG_SUCCESS, "Email confirme");

        response.put(KEY_MESSAGE, "Email confirmé. Votre compte est maintenant actif.");
        return response;
    }

    /**
     * Connexion securisee avec HMAC et emission de JWT.
     *
     * @param request requete de login
     * @return reponse avec JWT ou erreur
     */
    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            response.put(KEY_ERROR, "Email obligatoire");
            return response;
        }

        if (request.getNonce() == null || request.getNonce().isBlank()) {
            response.put(KEY_ERROR, "Nonce obligatoire");
            return response;
        }

        if (request.getTimestamp() <= 0) {
            response.put(KEY_ERROR, "Timestamp obligatoire");
            return response;
        }

        if (request.getHmac() == null || request.getHmac().isBlank()) {
            response.put(KEY_ERROR, "HMAC obligatoire");
            return response;
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            transactionLogService.log("LOGIN", request.getEmail(), LOG_FAILURE, "Utilisateur introuvable");
            response.put(KEY_ERROR, "Utilisateur introuvable");
            return response;
        }

        if (!user.isEmailVerified()) {
            transactionLogService.log("LOGIN", request.getEmail(), LOG_FAILURE, "Email non confirme");
            response.put(KEY_ERROR, "Email non confirmé. Veuillez vérifier votre boîte mail.");
            return response;
        }

        long now = System.currentTimeMillis() / 1000;
        long diff = Math.abs(now - request.getTimestamp());

        if (diff > 300) {
            transactionLogService.log("LOGIN", request.getEmail(), LOG_FAILURE, "Requete expiree");
            response.put(KEY_ERROR, "Requête expirée");
            return response;
        }

        if (authNonceRepository.findByUserAndNonce(user, request.getNonce()).isPresent()) {
            transactionLogService.log("LOGIN", request.getEmail(), LOG_FAILURE, "Nonce deja utilise");
            response.put(KEY_ERROR, "Nonce déjà utilisé");
            return response;
        }

        String decryptedPassword = passwordCryptoService.decrypt(user.getPasswordEncrypted());

        String message = hmacService.buildMessage(
                request.getEmail(),
                request.getNonce(),
                request.getTimestamp()
        );

        String expectedHmac = hmacService.hmacSha256(decryptedPassword, message);

        if (!constantTimeEquals(expectedHmac, request.getHmac())) {
            transactionLogService.log("LOGIN", request.getEmail(), LOG_FAILURE, "HMAC invalide");
            response.put(KEY_ERROR, "HMAC invalide");
            return response;
        }

        AuthNonce authNonce = new AuthNonce();
        authNonce.setUser(user);
        authNonce.setNonce(request.getNonce());
        authNonce.setCreatedAt(LocalDateTime.now());
        authNonce.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        authNonce.setConsumed(true);
        authNonceRepository.save(authNonce);

        transactionLogService.log("LOGIN", request.getEmail(), LOG_SUCCESS, "Connexion reussie");

        return issueToken(user);
    }

    /**
     * Retourne les informations de l utilisateur connecte.
     *
     * @param authorizationHeader header Authorization
     * @return informations utilisateur ou erreur
     */
    public Map<String, Object> getMe(String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            response.put(KEY_ERROR, MSG_TOKEN_INVALIDE);
            return response;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            response.put(KEY_ERROR, MSG_TOKEN_INVALIDE);
            return response;
        }

        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            response.put(KEY_ERROR, "Utilisateur non trouvé pour ce token");
            return response;
        }

        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("createdAt", user.getCreatedAt());
        response.put("emailVerified", user.isEmailVerified());
        response.put("publicKey", user.getPublicKey());

        return response;
    }

    /**
     * Change le mot de passe d un utilisateur authentifie.
     *
     * @param authorizationHeader header Authorization
     * @param request ancien et nouveau mot de passe
     * @return message de succes ou d erreur
     */
    public Map<String, Object> changePassword(String authorizationHeader, ChangePasswordRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            response.put(KEY_ERROR, MSG_TOKEN_INVALIDE);
            return response;
        }

        if (request == null) {
            response.put(KEY_ERROR, "Requête invalide");
            return response;
        }

        if (request.getOldPassword() == null || request.getOldPassword().isBlank()) {
            response.put(KEY_ERROR, "Ancien mot de passe obligatoire");
            return response;
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            response.put(KEY_ERROR, "Nouveau mot de passe obligatoire");
            return response;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            response.put(KEY_ERROR, MSG_TOKEN_INVALIDE);
            return response;
        }

        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            response.put(KEY_ERROR, "Utilisateur non trouvé pour ce token");
            return response;
        }

        String decryptedStoredPassword = passwordCryptoService.decrypt(user.getPasswordEncrypted());

        if (!decryptedStoredPassword.equals(request.getOldPassword())) {
            transactionLogService.log("CHANGE_PASSWORD", email, LOG_FAILURE, "Ancien mot de passe incorrect");
            response.put(KEY_ERROR, "Ancien mot de passe incorrect");
            return response;
        }

        if (!passwordPolicyValidator.isValid(request.getNewPassword())) {
            transactionLogService.log("CHANGE_PASSWORD", email, LOG_FAILURE, "Nouveau mot de passe invalide");
            response.put(KEY_ERROR, passwordPolicyValidator.getRulesMessage());
            return response;
        }

        String newEncryptedPassword = passwordCryptoService.encrypt(request.getNewPassword());
        user.setPasswordEncrypted(newEncryptedPassword);
        userRepository.save(user);

        transactionLogService.log("CHANGE_PASSWORD", email, LOG_SUCCESS, "Mot de passe change");

        response.put(KEY_MESSAGE, "Mot de passe changé avec succès");
        return response;
    }

    /**
     * Deconnexion d un utilisateur.
     *
     * @param authorizationHeader header Authorization
     * @return message de confirmation
     */
    public Map<String, Object> logout(String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            response.put(KEY_ERROR, MSG_TOKEN_INVALIDE);
            return response;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            response.put(KEY_ERROR, MSG_TOKEN_INVALIDE);
            return response;
        }

        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            transactionLogService.log("LOGOUT", email, LOG_FAILURE, "Utilisateur non trouve");
            response.put(KEY_ERROR, "Utilisateur non trouvé");
            return response;
        }

        user.setToken(null);
        user.setTokenExpiresAt(null);
        userRepository.save(user);

        transactionLogService.log("LOGOUT", email, LOG_SUCCESS, "Deconnexion reussie");

        response.put(KEY_MESSAGE, "Déconnexion réussie");
        return response;
    }

    /**
     * Genere un JWT pour un utilisateur authentifie.
     *
     * @param user utilisateur authentifie
     * @return reponse avec JWT
     */
    public Map<String, Object> issueToken(User user) {
        Map<String, Object> response = new HashMap<>();

        String token = jwtService.generateToken(user.getEmail());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtExpiration / 1000);

        user.setToken(token);
        user.setTokenExpiresAt(expiresAt);
        userRepository.save(user);

        response.put(KEY_MESSAGE, "Connexion réussie");
        response.put("accessToken", token);
        response.put("expiresAt", expiresAt);
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return response;
    }

    /**
     * Compare deux chaines en temps constant.
     *
     * @param a premiere chaine
     * @param b deuxieme chaine
     * @return true si identiques, sinon false
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }
}