package scene;

import database.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Bug Report and Feedback Scene
 * <p>
 * This class provides a scene for users to submit bug reports or suggestions.
 * The submitted message is associated with the logged-in user and saved to the database.
 * It includes a text area for input and buttons for submission and navigation.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class ReportScene {

    private final Scene scene;
    private final Stage stage;

    /**
     * Constructs the report scene.
     *
     * @param stage The primary stage of the application.
     */
    public ReportScene(Stage stage) {
        this.stage = stage;

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #016300;");

        Label title = new Label("Prijavi Grešku ili Sugestiju");
        title.setFont(Font.font("Arial", 26));
        title.setTextFill(Color.WHITE);

        TextArea reportArea = new TextArea();
        reportArea.setPromptText("Opišite problem ili vašu sugestiju ovde...");
        reportArea.setWrapText(true);
        reportArea.setMaxWidth(500);
        reportArea.setPrefHeight(200);

        Button submitButton = new Button("Pošalji");
        submitButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold;");
        submitButton.setOnAction(e -> handleSubmit(reportArea.getText()));

        Button backButton = new Button("Nazad na Meni");
        backButton.setOnAction(e -> stage.setScene(new MenuScene(stage).getScene()));

        VBox.setMargin(backButton, new Insets(20, 0, 0, 0));

        layout.getChildren().addAll(title, reportArea, submitButton, backButton);
        this.scene = new Scene(layout, 800, 600);
    }

    /**
     * Handles the submission of the report. It validates the message,
     * retrieves the current user's ID, saves the report to the database,
     * and provides feedback to the user.
     *
     * @param message The report content from the text area.
     */
    private void handleSubmit(String message) {
        if (message.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Poruka ne može biti prazna.").showAndWait();
            return;
        }

        int userId = DatabaseManager.INSTANCE.getCurrentUserId();
        if (userId == -1) {
            new Alert(Alert.AlertType.ERROR, "Niste prijavljeni. Prijavite se da biste poslali izveštaj.").showAndWait();
            stage.setScene(new LoginScene(stage).getScene());
            return;
        }

        boolean success = DatabaseManager.INSTANCE.saveReport(userId, message);

        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "Hvala! Vaš izveštaj je uspešno poslat.").showAndWait();
            stage.setScene(new MenuScene(stage).getScene());
        } else {
            new Alert(Alert.AlertType.ERROR, "Došlo je do greške prilikom slanja izveštaja.").showAndWait();
        }
    }

    /**
     * Returns the scene for the report submission screen.
     *
     * @return The constructed report scene.
     */
    public Scene getScene() {
        return scene;
    }
}
