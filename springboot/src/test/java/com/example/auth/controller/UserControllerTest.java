package com.example.auth.controller;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.TokenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Tests unitaires du UserController.
 * Vérifie la liste des utilisateurs avec et sans token valide.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserController userController;

    private User requester;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("Alice");
        requester.setEmail("alice@test.com");
        requester.setToken("token-alice");
        requester.setTokenExpiresAt(LocalDateTime.now().plusHours(1));

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Bob");
        otherUser.setEmail("bob@test.com");
    }

    // ── getUsers — token invalide ─────────────────────────────────────────────

    @Test
    void retourneErreurSiTokenNull() {
        when(tokenService.getUserFromToken(null)).thenReturn(null);

        Object result = userController.getUsers(null);

        Assertions.assertInstanceOf(Map.class, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        Assertions.assertTrue(map.containsKey("error"));
    }

    @Test
    void retourneErreurSiTokenInvalide() {
        when(tokenService.getUserFromToken("Bearer invalide")).thenReturn(null);

        Object result = userController.getUsers("Bearer invalide");

        Assertions.assertInstanceOf(Map.class, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        Assertions.assertTrue(map.containsKey("error"));
    }

    // ── getUsers — token valide ───────────────────────────────────────────────

    @Test
    void retourneListeUtilisateursAvecTokenValide() {
        when(tokenService.getUserFromToken("Bearer token-alice")).thenReturn(requester);
        when(userRepository.findAll()).thenReturn(Arrays.asList(requester, otherUser));

        Object result = userController.getUsers("Bearer token-alice");

        Assertions.assertInstanceOf(List.class, result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        Assertions.assertEquals(1, list.size());
    }

    @Test
    void exclutLeDemandeurDeLaListe() {
        when(tokenService.getUserFromToken("Bearer token-alice")).thenReturn(requester);
        when(userRepository.findAll()).thenReturn(Arrays.asList(requester, otherUser));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list =
                (List<Map<String, Object>>) userController.getUsers("Bearer token-alice");

        // requester (id=1) ne doit pas apparaître dans la liste
        boolean containsRequester = list.stream()
                .anyMatch(m -> m.get("id").equals(1L));
        Assertions.assertFalse(containsRequester);
    }

    @Test
    void inclutAutresUtilisateurs() {
        when(tokenService.getUserFromToken("Bearer token-alice")).thenReturn(requester);
        when(userRepository.findAll()).thenReturn(Arrays.asList(requester, otherUser));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list =
                (List<Map<String, Object>>) userController.getUsers("Bearer token-alice");

        Assertions.assertEquals("Bob", list.get(0).get("name"));
        Assertions.assertEquals("bob@test.com", list.get(0).get("email"));
    }

    @Test
    void retourneListeVideSiSeulUtilisateur() {
        when(tokenService.getUserFromToken("Bearer token-alice")).thenReturn(requester);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(requester));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list =
                (List<Map<String, Object>>) userController.getUsers("Bearer token-alice");

        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    void chaqueEntreeContientIdNameEmail() {
        when(tokenService.getUserFromToken("Bearer token-alice")).thenReturn(requester);
        when(userRepository.findAll()).thenReturn(Arrays.asList(requester, otherUser));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list =
                (List<Map<String, Object>>) userController.getUsers("Bearer token-alice");

        Map<String, Object> entry = list.get(0);
        Assertions.assertTrue(entry.containsKey("id"));
        Assertions.assertTrue(entry.containsKey("name"));
        Assertions.assertTrue(entry.containsKey("email"));
    }
}
