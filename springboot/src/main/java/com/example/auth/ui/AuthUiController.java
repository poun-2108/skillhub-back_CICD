package com.example.auth.ui;

import com.example.auth.validator.PasswordPolicyValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Controleur JavaFX de l'interface d'authentification.
 * Cette classe gere :
 * l'inscription,
 * la connexion,
 * la verification visuelle du mot de passe,
 * et les appels HTTP vers l'API Spring Boot.
 */
public class AuthUiController {

    /**
     * URL de base de l'API d'authentification.
     */
    private static final String API_URL = "http://localhost:8000/api/auth";

    /**
     * Style rouge pour les messages d'erreur.
     */
    private static final String STYLE_RED = "-fx-text-fill: red;";

    /**
     * Style vert pour les messages de succes.
     */
    private static final String STYLE_GREEN = "-fx-text-fill: green;";

    /**
     * Style orange pour les messages moyens.
     */
    private static final String STYLE_ORANGE = "-fx-text-fill: orange;";

    /**
     * Validateur de mot de passe.
     */
    private final PasswordPolicyValidator passwordPolicyValidator = new PasswordPolicyValidator();

    /**
     * Champ pour saisir le nom.
     */
    @FXML
    private TextField nameField;

    /**
     * Champ pour saisir l'email.
     */
    @FXML
    private TextField emailField;

    /**
     * Champ pour saisir le mot de passe.
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Champ pour confirmer le mot de passe.
     */
    @FXML
    private PasswordField passwordConfirmField;

    /**
     * Label pour afficher la force du mot de passe.
     */
    @FXML
    private Label passwordStrengthLabel;

    /**
     * Label pour afficher si les mots de passe correspondent.
     */
    @FXML
    private Label passwordMatchLabel;

    /**
     * Label pour afficher les messages a l'utilisateur.
     */
    @FXML
    private Label messageLabel;

    /**
     * Methode appelee automatiquement au chargement de l'interface.
     * Elle ajoute des ecouteurs pour mettre a jour
     * la force du mot de passe et la confirmation.
     */
    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength();
            updatePasswordMatch();
        });

        passwordConfirmField.textProperty().addListener((observable, oldValue, newValue) ->
                updatePasswordMatch()
        );
    }

    /**
     * Verifie si une chaine est vide.
     *
     * @param value valeur a tester
     * @return true si la valeur est nulle ou vide
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Gere l'inscription de l'utilisateur.
     * Verifie les champs, compare les mots de passe,
     * controle la validite du mot de passe
     * puis envoie la requete a l'API.
     */
    @FXML
    public void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String passwordConfirm = passwordConfirmField.getText();

        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(passwordConfirm)) {
            messageLabel.setText("Nom, email, mot de passe et confirmation obligatoires");
            messageLabel.setStyle(STYLE_RED);
            return;
        }

        if (!password.equals(passwordConfirm)) {
            messageLabel.setText("Les mots de passe ne sont pas identiques");
            messageLabel.setStyle(STYLE_RED);
            return;
        }

        if (!passwordPolicyValidator.isValid(password)) {
            messageLabel.setText("Mot de passe trop faible");
            messageLabel.setStyle(STYLE_RED);
            return;
        }

        String json = "{"
                + "\"name\":\"" + escapeJson(name) + "\","
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"password\":\"" + escapeJson(password) + "\""
                + "}";

        try {
            String result = sendPost(API_URL + "/register", json);
            messageLabel.setText("Inscription reussie : " + result);
            messageLabel.setStyle(STYLE_GREEN);
        } catch (Exception e) {
            messageLabel.setText("Erreur inscription : " + e.getMessage());
            messageLabel.setStyle(STYLE_RED);
        }
    }

    /**
     * Gere la connexion de l'utilisateur.
     * Verifie les champs obligatoires
     * puis envoie la requete de connexion a l'API.
     */
    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (isBlank(email) || isBlank(password)) {
            messageLabel.setText("Email et mot de passe obligatoires");
            messageLabel.setStyle(STYLE_RED);
            return;
        }

        String json = "{"
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"password\":\"" + escapeJson(password) + "\""
                + "}";

        try {
            String result = sendPost(API_URL + "/login", json);
            messageLabel.setText("Connexion reussie : " + result);
            messageLabel.setStyle(STYLE_GREEN);
        } catch (Exception e) {
            messageLabel.setText("Erreur connexion : " + e.getMessage());
            messageLabel.setStyle(STYLE_RED);
        }
    }

    /**
     * Met a jour l'affichage de la force du mot de passe.
     * La force depend de la validite du mot de passe
     * et de sa longueur.
     */
    private void updatePasswordStrength() {
        String password = passwordField.getText();

        if (password == null) {
            password = "";
        }

        if (!passwordPolicyValidator.isValid(password)) {
            passwordStrengthLabel.setText("Force : faible");
            passwordStrengthLabel.setStyle(STYLE_RED);
            return;
        }

        if (password.length() >= 16) {
            passwordStrengthLabel.setText("Force : forte");
            passwordStrengthLabel.setStyle(STYLE_GREEN);
        } else {
            passwordStrengthLabel.setText("Force : moyenne");
            passwordStrengthLabel.setStyle(STYLE_ORANGE);
        }
    }

    /**
     * Met a jour l'affichage de correspondance
     * entre le mot de passe et sa confirmation.
     */
    private void updatePasswordMatch() {
        String password = passwordField.getText();
        String confirm = passwordConfirmField.getText();

        if (isBlank(confirm)) {
            passwordMatchLabel.setText("");
            return;
        }

        if (password != null && password.equals(confirm)) {
            passwordMatchLabel.setText("Les mots de passe correspondent");
            passwordMatchLabel.setStyle(STYLE_GREEN);
        } else {
            passwordMatchLabel.setText("Les mots de passe sont differents");
            passwordMatchLabel.setStyle(STYLE_RED);
        }
    }

    /**
     * Envoie une requete HTTP POST a l'API.
     *
     * @param urlText adresse de destination
     * @param jsonBody corps JSON a envoyer
     * @return corps de la reponse HTTP
     * @throws IOException si la requete echoue ou si l'API retourne une erreur
     */
    private String sendPost(String urlText, String jsonBody) throws IOException {
        URL url = new URL(urlText);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = connection.getResponseCode();

        InputStream stream;
        if (code >= 200 && code < 300) {
            stream = connection.getInputStream();
        } else {
            stream = connection.getErrorStream();
        }

        String responseBody = readStream(stream);

        if (code >= 200 && code < 300) {
            return responseBody;
        }

        throw new IOException("HTTP " + code + " : " + responseBody);
    }

    /**
     * Lit un flux de donnees et retourne son contenu sous forme de texte.
     *
     * @param stream flux a lire
     * @return texte lu dans le flux
     * @throws IOException si une erreur de lecture se produit
     */
    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );

        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    /**
     * Echappe les caracteres sensibles dans une chaine
     * avant de construire un texte JSON.
     *
     * @param text texte a proteger
     * @return texte echappe
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}