package com.example.auth.repository;

import com.example.auth.entity.AuthNonce;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour gérer les nonces d'authentification.
 *
 * @author Poun
 * @version 3.3
 */
public interface AuthNonceRepository extends JpaRepository<AuthNonce, Long> {

    /**
     * Recherche un nonce par utilisateur et valeur.
     */
    Optional<AuthNonce> findByUserAndNonce(User user, String nonce);
}