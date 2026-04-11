package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service de validation du token Bearer.
 * Centralise la logique de verification pour eviter la duplication.
 *
 * @author Nirina
 * @version 1.0
 */
@Service
public class TokenService {

    /** Repository utilisateur. */
    private final UserRepository userRepository;

    /**
     * Constructeur.
     *
     * @param userRepository repository utilisateur
     */
    public TokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Extrait et valide le token Bearer depuis le header Authorization.
     * Retourne l utilisateur si le token est valide, null sinon.
     *
     * @param authorizationHeader header Authorization
     * @return utilisateur valide ou null
     */
    public User getUserFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7);
        User user = userRepository.findByToken(token).orElse(null);

        if (user == null) {
            return null;
        }

        if (user.getTokenExpiresAt() == null ||
                user.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        return user;
    }

    /**
     * Verifie si le header Authorization est valide (format Bearer).
     *
     * @param authorizationHeader header Authorization
     * @return true si le format est valide
     */
    public boolean hasValidFormat(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }
}