package com.example.auth.controller;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller pour la gestion du profil utilisateur.
 * Permet l upload et la recuperation de la photo de profil.
 *
 * @author Nirina
 * @version 1.1
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    /** Dossier de stockage des avatars. */
    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    /** URL de base de l application. */
    @Value("${app.base.url:http://localhost:8000}")
    private String baseUrl;

    /** Repository utilisateur. */
    private final UserRepository userRepository;

    /** Service de validation du token. */
    private final TokenService tokenService;

    /**
     * Constructeur.
     *
     * @param userRepository repository utilisateur
     * @param tokenService service token
     */
    public ProfileController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * Upload la photo de profil de l utilisateur connecte.
     *
     * @param authorizationHeader header Authorization
     * @param file fichier image a uploader
     * @return URL de la photo ou erreur
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadAvatar(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("file") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();

        User user = tokenService.getUserFromToken(authorizationHeader);

        if (user == null) {
            response.put("error", "Token manquant, invalide ou expire");
            return response;
        }

        if (file == null || file.isEmpty()) {
            response.put("error", "Fichier vide");
            return response;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("error", "Seules les images sont acceptees");
            return response;
        }

        try {
            // Creation du dossier si necessaire
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generation d un nom unique pour le fichier
            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, filename);

            // Sauvegarde du fichier
            Files.write(filePath, file.getBytes());

            // Mise a jour de l avatar en base
            String avatarUrl = baseUrl + "/api/profile/avatar/" + filename;
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            response.put("message", "Photo de profil mise a jour");
            response.put("avatarUrl", avatarUrl);

        } catch (IOException e) {
            response.put("error", "Erreur lors de l upload : " + e.getMessage());
        }

        return response;
    }

    /**
     * Retourne le profil complet de l utilisateur connecte.
     *
     * @param authorizationHeader header Authorization
     * @return infos profil avec avatar
     */
    @GetMapping("/me")
    public Map<String, Object> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Map<String, Object> response = new HashMap<>();

        User user = tokenService.getUserFromToken(authorizationHeader);

        if (user == null) {
            response.put("error", "Token manquant, invalide ou expire");
            return response;
        }

        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("avatar", user.getAvatar());
        response.put("createdAt", user.getCreatedAt());

        return response;
    }

    /**
     * Retourne l extension d un fichier.
     *
     * @param filename nom du fichier
     * @return extension avec le point
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}