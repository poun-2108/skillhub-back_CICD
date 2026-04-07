package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour gérer les utilisateurs.
 *
 * @author Poun
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par email.
     *
     * @param email email recherché
     * @return utilisateur optionnel
     */
    Optional<User> findByEmail(String email);

    /**
     * Recherche un utilisateur par token.
     *
     * @param token token recherché
     * @return utilisateur optionnel
     */
    Optional<User> findByToken(String token);
}