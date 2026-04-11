package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour gerer les utilisateurs.
 *
 * @author Nirina
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par email.
     *
     * @param email email recherche
     * @return utilisateur optionnel
     */
    Optional<User> findByEmail(String email);

    /**
     * Recherche un utilisateur par token JWT.
     *
     * @param token token recherche
     * @return utilisateur optionnel
     */
    Optional<User> findByToken(String token);

    /**
     * Recherche un utilisateur par token de verification email.
     *
     * @param emailVerificationToken token de verification
     * @return utilisateur optionnel
     */
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);
}