package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;

/**
 * Tests unitaires du TokenService.
 * Vérifie la validation du header Authorization Bearer.
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenService tokenService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setToken("token-valide");
        validUser.setTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        validUser.setEmail("user@test.com");
    }

    // ── getUserFromToken ──────────────────────────────────────────────────────

    @Test
    void retourneUtilisateurAvecTokenValide() {
        when(userRepository.findByToken("token-valide")).thenReturn(Optional.of(validUser));

        User result = tokenService.getUserFromToken("Bearer token-valide");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("user@test.com", result.getEmail());
    }

    @Test
    void retourneNullSiHeaderNull() {
        User result = tokenService.getUserFromToken(null);
        Assertions.assertNull(result);
    }

    @Test
    void retourneNullSiHeaderSansBearerPrefix() {
        User result = tokenService.getUserFromToken("token-sans-bearer");
        Assertions.assertNull(result);
    }

    @Test
    void retourneNullSiHeaderVideApresBearer() {
        // Le token extrait sera vide, findByToken retourne empty
        when(userRepository.findByToken("")).thenReturn(Optional.empty());
        User result = tokenService.getUserFromToken("Bearer ");
        Assertions.assertNull(result);
    }

    @Test
    void retourneNullSiTokenInconnu() {
        when(userRepository.findByToken("token-inconnu")).thenReturn(Optional.empty());

        User result = tokenService.getUserFromToken("Bearer token-inconnu");
        Assertions.assertNull(result);
    }

    @Test
    void retourneNullSiTokenExpire() {
        validUser.setTokenExpiresAt(LocalDateTime.now().minusMinutes(10));
        when(userRepository.findByToken("token-valide")).thenReturn(Optional.of(validUser));

        User result = tokenService.getUserFromToken("Bearer token-valide");
        Assertions.assertNull(result);
    }

    @Test
    void retourneNullSiTokenExpiresAtNull() {
        validUser.setTokenExpiresAt(null);
        when(userRepository.findByToken("token-valide")).thenReturn(Optional.of(validUser));

        User result = tokenService.getUserFromToken("Bearer token-valide");
        Assertions.assertNull(result);
    }

    // ── hasValidFormat ────────────────────────────────────────────────────────

    @Test
    void formatValideAvecBearer() {
        Assertions.assertTrue(tokenService.hasValidFormat("Bearer abc123"));
    }

    @Test
    void formatInvalideSansBearer() {
        Assertions.assertFalse(tokenService.hasValidFormat("abc123"));
    }

    @Test
    void formatInvalideSiNull() {
        Assertions.assertFalse(tokenService.hasValidFormat(null));
    }

    @Test
    void formatInvalideSiChainVide() {
        Assertions.assertFalse(tokenService.hasValidFormat(""));
    }
}
