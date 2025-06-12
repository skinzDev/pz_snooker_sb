package com.example.pz;

import database.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import scene.LoginScene;

/**
 * The main entry point for the Snooker Score Tracker JavaFX application.
 * This class is responsible for initializing the application, setting up the
 * primary stage, and handling the application lifecycle.
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class HelloApplication extends Application {

    /**
     * The main entry point for all JavaFX applications. The start method is
     * called after the init method has returned, and after the system is ready
     * for the application to begin running.
     *
     * @param stage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage stage) {
        try {
            DatabaseManager.INSTANCE.initialize();

            Scene loginScene = new LoginScene(stage).getScene();
            stage.setTitle("Snooker Score Tracker");
            stage.setScene(loginScene);
            stage.setResizable(false);
            stage.show();

            stage.setOnCloseRequest(e -> {
                DatabaseManager.INSTANCE.disconnect();
                Platform.exit();
            });

        } catch (Exception e) {
            System.err.println("Fatal error during application startup: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Startup Error",
                    "The application could not be started due to a database issue.\n\nError details: " + e.getMessage());
            Platform.exit();
        }
    }

    /**
     * Displays a modal error alert dialog to the user.
     *
     * @param title   The title of the alert dialog window.
     * @param message The error message to be displayed.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * The main method, which is used to launch the JavaFX application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
