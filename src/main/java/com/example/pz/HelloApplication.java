package com.example.pz;

import database.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import scene.LoginScene;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Inicijalizuj i popravi bazu podataka pri pokretanju
            DatabaseManager.INSTANCE.initialize();

            // Podesi i prikaži početnu scenu za prijavu
            Scene loginScene = new LoginScene(stage).getScene();
            stage.setTitle("Snooker Score Tracker");
            stage.setScene(loginScene);
            stage.setResizable(false); // Onemogući promenu veličine prozora
            stage.show();

            // Osiguraj da se konekcija sa bazom prekine prilikom zatvaranja aplikacije
            stage.setOnCloseRequest(e -> {
                DatabaseManager.INSTANCE.disconnect();
                Platform.exit();
            });

        } catch (Exception e) {
            System.err.println("Fatalna greška prilikom pokretanja aplikacije: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Greška pri pokretanju",
                    "Aplikacija se ne može pokrenuti. Problem je sa bazom podataka.\n\nDetalji greške: " + e.getMessage());
            Platform.exit();
        }
    }


    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static void main(String[] args) {
        launch(args);
    }
}