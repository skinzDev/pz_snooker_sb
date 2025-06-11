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

public class LoginScene {
    private final Scene scene;

    public LoginScene(Stage stage) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #016300;");

        Label title = new Label("Dobrodošli");
        title.setFont(Font.font("Arial", 28));
        title.setStyle("-fx-text-fill: white;");

        TextField username = new TextField();
        username.setPromptText("Korisničko ime");
        username.setMaxWidth(300);

        PasswordField password = new PasswordField();
        password.setPromptText("Lozinka");
        password.setMaxWidth(300);

        Button loginBtn = new Button("Prijavi se");
        loginBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold;");
        loginBtn.setPrefWidth(300);
        loginBtn.setOnAction(e -> {
            if (DatabaseManager.INSTANCE.validateUser(username.getText(), password.getText())) {
                stage.setScene(new MenuScene(stage).getScene());
            } else {
                new Alert(Alert.AlertType.ERROR, "Korisničko ime ili lozinka nisu ispravni.").showAndWait();
            }
        });

        Hyperlink registerLink = new Hyperlink("Nemate nalog? Registrujte se!");
        registerLink.setTextFill(Color.WHITE);
        registerLink.setOnAction(e -> stage.setScene(new RegisterScene(stage).getScene()));

        layout.getChildren().addAll(title, username, password, loginBtn, registerLink);
        this.scene = new Scene(layout, 800, 600);
    }

    public Scene getScene() {
        return scene;
    }
}