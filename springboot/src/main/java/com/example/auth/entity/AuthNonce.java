package com.example.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Entité représentant un nonce d'authentification.
 *
 * Le nonce sert à empêcher les attaques par rejeu.
 *
 * @author Poun
 * @version 3.1
 */
@Entity
@Table(
        name = "auth_nonce",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "nonce"})
        }
)
public class AuthNonce {

    /**
     * Identifiant unique du nonce.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Utilisateur lié au nonce.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Valeur du nonce.
     */
    @Column(nullable = false, length = 255)
    private String nonce;

    /**
     * Date d'expiration du nonce.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Indique si le nonce a été consommé.
     */
    @Column(nullable = false)
    private boolean consumed;

    /**
     * Date de création.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Constructeur vide requis par JPA.
     * ✅ Fix java:S1186 — commentaire explicatif ajouté
     */
    public AuthNonce() {
        // Requis par JPA pour l'instanciation des entités
    }

    /**
     * Retourne l'identifiant.
     *
     * @return id du nonce
     */
    public Long getId() {
        return id;
    }

    /**
     * Modifie l'identifiant.
     *
     * @param id nouvel identifiant
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retourne l'utilisateur.
     *
     * @return utilisateur lié
     */
    public User getUser() {
        return user;
    }

    /**
     * Modifie l'utilisateur.
     *
     * @param user nouvel utilisateur
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Retourne le nonce.
     *
     * @return nonce
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Modifie le nonce.
     *
     * @param nonce nouvelle valeur
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * Retourne la date d'expiration.
     *
     * @return date d'expiration
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Modifie la date d'expiration.
     *
     * @param expiresAt nouvelle date
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Indique si le nonce a été consommé.
     *
     * @return true si consommé
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Modifie l'état du nonce.
     *
     * @param consumed nouvel état
     */
    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    /**
     * Retourne la date de création.
     *
     * @return date de création
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Modifie la date de création.
     *
     * @param createdAt nouvelle date
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}