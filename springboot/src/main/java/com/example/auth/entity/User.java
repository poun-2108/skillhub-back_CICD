package com.example.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entite representant un utilisateur.
 *
 * Moustass CloudSec :
 * - mot de passe chiffre AES/GCM
 * - JWT pour l authentification
 * - email de confirmation obligatoire
 * - paire de cles RSA generee a l inscription
 *
 * SkillHub :
 * - role apprenant ou formateur
 *
 * @author Nirina
 * @version 1.2
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Identifiant unique de l utilisateur.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de l utilisateur.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Email unique de l utilisateur.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Role de l utilisateur.
     * Valeurs attendues : apprenant ou formateur.
     */
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * Mot de passe chiffre de maniere reversible AES/GCM.
     */
    @Column(name = "password_encrypted", nullable = false, length = 500)
    private String passwordEncrypted;

    /**
     * JWT stocke temporairement.
     */
    @Column(length = 500)
    private String token;

    /**
     * Date d expiration du token.
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    /**
     * Indique si l email a ete verifie.
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /**
     * Token de verification email.
     */
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    /**
     * Cle publique RSA de l utilisateur en Base64.
     * Partageable avec les autres utilisateurs.
     */
    @Column(name = "public_key", length = 1000)
    private String publicKey;

    /**
     * Cle privee RSA de l utilisateur en Base64.
     * Stockee de facon securisee, jamais exposee.
     */
    @Column(name = "private_key", length = 4000)
    private String privateKey;

    /**
     * URL de la photo de profil.
     */
    @Column(length = 500)
    private String avatar;

    /**
     * Date de creation du compte.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Constructeur vide requis par JPA.
     */
    public User() {
        // Requis par JPA pour l instanciation des entites
    }

    /**
     * Retourne l identifiant.
     *
     * @return id utilisateur
     */
    public Long getId() {
        return id;
    }

    /**
     * Modifie l identifiant.
     *
     * @param id nouvel identifiant
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retourne le nom.
     *
     * @return nom utilisateur
     */
    public String getName() {
        return name;
    }

    /**
     * Modifie le nom.
     *
     * @param name nouveau nom
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne l email.
     *
     * @return email utilisateur
     */
    public String getEmail() {
        return email;
    }

    /**
     * Modifie l email.
     *
     * @param email nouvel email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retourne le role.
     *
     * @return role utilisateur
     */
    public String getRole() {
        return role;
    }

    /**
     * Modifie le role.
     *
     * @param role nouveau role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Retourne le mot de passe chiffre.
     *
     * @return mot de passe chiffre
     */
    public String getPasswordEncrypted() {
        return passwordEncrypted;
    }

    /**
     * Modifie le mot de passe chiffre.
     *
     * @param passwordEncrypted nouvelle valeur chiffree
     */
    public void setPasswordEncrypted(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    /**
     * Retourne le token JWT.
     *
     * @return token
     */
    public String getToken() {
        return token;
    }

    /**
     * Modifie le token JWT.
     *
     * @param token nouveau token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Retourne la date d expiration du token.
     *
     * @return date d expiration
     */
    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    /**
     * Modifie la date d expiration du token.
     *
     * @param tokenExpiresAt nouvelle date d expiration
     */
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    /**
     * Indique si l email est verifie.
     *
     * @return true si email verifie
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Modifie l etat de verification de l email.
     *
     * @param emailVerified nouvel etat
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Retourne le token de verification email.
     *
     * @return token de verification
     */
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    /**
     * Modifie le token de verification email.
     *
     * @param emailVerificationToken nouveau token
     */
    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    /**
     * Retourne la cle publique RSA.
     *
     * @return cle publique en Base64
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Modifie la cle publique RSA.
     *
     * @param publicKey nouvelle cle publique
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Retourne la cle privee RSA.
     *
     * @return cle privee en Base64
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Modifie la cle privee RSA.
     *
     * @param privateKey nouvelle cle privee
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Retourne l URL de la photo de profil.
     *
     * @return url avatar
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Modifie l URL de la photo de profil.
     *
     * @param avatar nouvelle url
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * Retourne la date de creation.
     *
     * @return date de creation
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Modifie la date de creation.
     *
     * @param createdAt nouvelle date
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}