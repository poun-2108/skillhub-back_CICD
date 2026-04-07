package com.example.auth.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale de l'application JavaFX TP_5.
 * Elle lance l'interface graphique d'authentification.
 *
 * @author Poun
 * @version 5.0
 */
public class AuthUiApplication extends Application {

    /**
     * Methode appelee au demarrage de l'application JavaFX.
     * Elle charge le fichier FXML et affiche la fenetre principale.
     *
     * @param stage fenetre principale de l'application
     * @throws Exception si erreur lors du chargement du FXML
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth-view.fxml"));
        Scene scene = new Scene(loader.load(), 420, 300);

        stage.setTitle("TP_5 - Authentification securisee");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Methode principale pour lancer l'application.
     *
     * @param args arguments du programme
     */
    public static void main(String[] args) {
        launch(args);
    }
}