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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du ProfileController.
 * Vérifie le profil et l'upload d'avatar.
 */
@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private ProfileController profileController;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(profileController, "uploadDir", "target/test-uploads");
        ReflectionTestUtils.setField(profileController, "baseUrl", "http://localhost:8000");

        user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@test.com");
        user.setAvatar("http://localhost:8000/api/profile/avatar/old.jpg");
        user.setCreatedAt(LocalDateTime.now());
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    void retourneProfilAvecTokenValide() {
        when(tokenService.getUserFromToken("Bearer token")).thenReturn(user);

        Map<String, Object> result = profileController.getProfile("Bearer token");

        Assertions.assertFalse(result.containsKey("error"));
        Assertions.assertEquals("Alice", result.get("name"));
        Assertions.assertEquals("alice@test.com", result.get("email"));
    }

    @Test
    void retourneErreurProfilSiTokenNull() {
        when(tokenService.getUserFromToken(null)).thenReturn(null);

        Map<String, Object> result = profileController.getProfile(null);

        Assertions.assertTrue(result.containsKey("error"));
    }

    @Test
    void retourneErreurProfilSiTokenInvalide() {
        when(tokenService.getUserFromToken("Bearer mauvais")).thenReturn(null);

        Map<String, Object> result = profileController.getProfile("Bearer mauvais");

        Assertions.assertTrue(result.containsKey("error"));
    }

    @Test
    void profilContientTousLesChamps() {
        when(tokenService.getUserFromToken("Bearer token")).thenReturn(user);

        Map<String, Object> result = profileController.getProfile("Bearer token");

        Assertions.assertTrue(result.containsKey("id"));
        Assertions.assertTrue(result.containsKey("name"));
        Assertions.assertTrue(result.containsKey("email"));
        Assertions.assertTrue(result.containsKey("avatar"));
        Assertions.assertTrue(result.containsKey("createdAt"));
    }

    // ── uploadAvatar ──────────────────────────────────────────────────────────

    @Test
    void retourneErreurAvatarSiTokenInvalide() {
        when(tokenService.getUserFromToken(null)).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        Map<String, Object> result = profileController.uploadAvatar(null, file);

        Assertions.assertTrue(result.containsKey("error"));
    }

    @Test
    void retourneErreurSiFichierVide() {
        when(tokenService.getUserFromToken("Bearer token")).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[0]);

        Map<String, Object> result = profileController.uploadAvatar("Bearer token", file);

        Assertions.assertTrue(result.containsKey("error"));
        Assertions.assertTrue(result.get("error").toString().contains("vide"));
    }

    @Test
    void retourneErreurSiTypeNonImage() {
        when(tokenService.getUserFromToken("Bearer token")).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[]{1, 2, 3});

        Map<String, Object> result = profileController.uploadAvatar("Bearer token", file);

        Assertions.assertTrue(result.containsKey("error"));
        Assertions.assertTrue(result.get("error").toString().contains("images"));
    }

    @Test
    void uploadeAvatarAvecSucces() {
        when(tokenService.getUserFromToken("Bearer token")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8});

        Map<String, Object> result = profileController.uploadAvatar("Bearer token", file);

        Assertions.assertFalse(result.containsKey("error"), "Ne doit pas avoir d'erreur");
        Assertions.assertTrue(result.containsKey("avatarUrl"));
        String avatarUrl = (String) result.get("avatarUrl");
        Assertions.assertTrue(avatarUrl.startsWith("http://localhost:8000"));
    }

    @Test
    void avatarUrlContientExtensionFichier() {
        when(tokenService.getUserFromToken("Bearer token")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", new byte[]{1, 2, 3});

        Map<String, Object> result = profileController.uploadAvatar("Bearer token", file);

        String avatarUrl = (String) result.get("avatarUrl");
        Assertions.assertTrue(avatarUrl.endsWith(".png"));
    }

    @Test
    void avatarUrlUtiliseJpgParDefautSiPasExtension() {
        when(tokenService.getUserFromToken("Bearer token")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo", "image/jpeg", new byte[]{1, 2, 3});

        Map<String, Object> result = profileController.uploadAvatar("Bearer token", file);

        String avatarUrl = (String) result.get("avatarUrl");
        Assertions.assertTrue(avatarUrl.endsWith(".jpg"));
    }
}
