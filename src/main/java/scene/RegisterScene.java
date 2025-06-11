package scene;

import database.DatabaseManager;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class RegisterScene {
    private final Scene scene;

    public RegisterScene(Stage stage) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #016300");

        Label title = new Label("Registracija");
        title.setFont(Font.font("Arial", 28));
        title.setStyle("-fx-text-fill: white");

        TextField username = new TextField();
        username.setPromptText("Unesite korisničko ime");
        username.setMaxWidth(300);

        PasswordField password = new PasswordField();
        password.setPromptText("Unesite lozinku");
        password.setMaxWidth(300);

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Potvrdite lozinku");
        confirmPassword.setMaxWidth(300);

        Button signUpBtn = new Button("Kreiraj nalog");
        signUpBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold;");
        signUpBtn.setPrefWidth(300);
        signUpBtn.setOnAction(e -> {
            String pass = password.getText();
            String user = username.getText();

            if (user.isBlank() || pass.isBlank()) {
                new Alert(Alert.AlertType.ERROR, "Sva polja su obavezna!").showAndWait();
                return;
            }
            if (!pass.equals(confirmPassword.getText())) {
                new Alert(Alert.AlertType.ERROR, "Lozinke se ne poklapaju!").showAndWait();
                return;
            }

            if (DatabaseManager.INSTANCE.registerUser(user, pass)) {
                new Alert(Alert.AlertType.INFORMATION, "Registracija uspešna! Možete se prijaviti.").showAndWait();
                stage.setScene(new LoginScene(stage).getScene());
            } else {
                new Alert(Alert.AlertType.ERROR, "Korisničko ime već postoji ili je došlo do greške.").showAndWait();
            }
        });

        Hyperlink loginLink = new Hyperlink("Već imate nalog? Prijavite se!");
        loginLink.setTextFill(Color.WHITE);
        loginLink.setOnAction(e -> stage.setScene(new LoginScene(stage).getScene()));

        layout.getChildren().addAll(title, username, password, confirmPassword, signUpBtn, loginLink);
        this.scene = new Scene(layout, 800, 600);
    }

    public Scene getScene() {
        return scene;
    }
}