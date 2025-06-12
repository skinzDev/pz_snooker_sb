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
 * User Login Scene
 * <p>
 * This class creates and manages the user login screen. It provides UI components
 * for username and password input, a login button for authentication, a hyperlink
 * to the registration scene, and a label to open the Terms and Conditions.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class LoginScene {
    private final Scene scene;

    /**
     * Constructs the login scene.
     *
     * @param stage The primary stage of the application.
     */
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

        Label tocLbl = new Label("Read Terms & Conditions");
        tocLbl.setFont(Font.font("Arial", 12));
        tocLbl.setStyle("-fx-text-fill: white;");
        tocLbl.setOnMouseClicked(e -> new TermsAndConditionsApplication().launch());

        layout.getChildren().addAll(title, username, password, loginBtn, registerLink, tocLbl);
        this.scene = new Scene(layout, 800, 600);
    }

    /**
     * Returns the scene for the login screen.
     *
     * @return The constructed login scene.
     */
    public Scene getScene() {
        return scene;
    }
}
