package com.example.auth.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tests unitaires du TransactionLogService.
 * Vérifie l'écriture des logs dans le fichier transactions.log.
 */
class TransactionLogServiceTest {

    private TransactionLogService logService;

    /** Chemin du fichier de log utilisé par le service. */
    private static final Path LOG_PATH = Paths.get("logs/transactions.log");

    @BeforeEach
    void setUp() throws IOException {
        logService = new TransactionLogService();
        // Supprimer le fichier de log s'il existe déjà
        Files.deleteIfExists(LOG_PATH);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(LOG_PATH);
    }

    // ── log ───────────────────────────────────────────────────────────────────

    @Test
    void ecritUneLigneDansLeFichier() throws IOException {
        logService.log("REGISTER", "user@test.com", "SUCCESS", "test detail");

        Assertions.assertTrue(LOG_PATH.toFile().exists(), "Le fichier de log doit être créé");

        List<String> lines = Files.readAllLines(LOG_PATH);
        Assertions.assertEquals(1, lines.size());
    }

    @Test
    void ligneContientActionEmailStatut() throws IOException {
        logService.log("LOGIN", "alice@test.com", "SUCCESS", "detail");

        List<String> lines = Files.readAllLines(LOG_PATH);
        String line = lines.get(0);

        Assertions.assertTrue(line.contains("LOGIN"));
        Assertions.assertTrue(line.contains("alice@test.com"));
        Assertions.assertTrue(line.contains("SUCCESS"));
    }

    @Test
    void ligneContientLeDetail() throws IOException {
        logService.log("LOGOUT", "bob@test.com", "SUCCESS", "detail-specifique");

        List<String> lines = Files.readAllLines(LOG_PATH);
        Assertions.assertTrue(lines.get(0).contains("detail-specifique"));
    }

    @Test
    void ligneContientTimestamp() throws IOException {
        logService.log("REGISTER", "user@test.com", "SUCCESS", "");

        List<String> lines = Files.readAllLines(LOG_PATH);
        // Le timestamp a le format [yyyy-MM-dd HH:mm:ss]
        Assertions.assertTrue(lines.get(0).matches("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\].*"));
    }

    @Test
    void appendeMultipleAppels() throws IOException {
        logService.log("REGISTER", "user1@test.com", "SUCCESS", "");
        logService.log("LOGIN", "user2@test.com", "FAILURE", "");
        logService.log("LOGOUT", "user3@test.com", "SUCCESS", "");

        List<String> lines = Files.readAllLines(LOG_PATH);
        Assertions.assertEquals(3, lines.size());
    }

    @Test
    void creedossierLogsAutomatiquement() throws IOException {
        // Supprimer le dossier logs si vide
        File logsDir = new File("logs");
        if (logsDir.exists() && logsDir.isDirectory() && logsDir.listFiles() != null
                && logsDir.listFiles().length == 0) {
            logsDir.delete();
        }

        logService.log("TEST", "user@test.com", "SUCCESS", "");

        Assertions.assertTrue(logsDir.exists());
    }

    @Test
    void formatContientCrochets() throws IOException {
        logService.log("VERIFY", "user@test.com", "SUCCESS", "detail");

        List<String> lines = Files.readAllLines(LOG_PATH);
        String line = lines.get(0);
        // Format : [timestamp] [action] [email] [statut] detail
        Assertions.assertTrue(line.contains("[VERIFY]"));
        Assertions.assertTrue(line.contains("[user@test.com]"));
        Assertions.assertTrue(line.contains("[SUCCESS]"));
    }
}
