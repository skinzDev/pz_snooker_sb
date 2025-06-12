package scene;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Main Menu and Game Settings Scene
 * <p>
 * This class provides the main menu interface for the application.
 * It allows users to configure match settings, such as player names and the number
 * of red balls, and provides navigation to start a new game, view match history,
 * or report a bug.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class MenuScene {
    private final Scene scene;
    private int brojCrvenih = 15;

    /**
     * Constructs the main menu scene.
     *
     * @param stage The primary stage of the application.
     */
    public MenuScene(Stage stage) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #016300");

        Label title = new Label("Podešavanja meča");
        title.setFont(Font.font("Arial", 26));
        title.setTextFill(Color.WHITE);

        GridPane playersGrid = new GridPane();
        playersGrid.setAlignment(Pos.CENTER);
        playersGrid.setHgap(10);
        playersGrid.setVgap(10);

        TextField player1Name = new TextField("Igrač 1");
        TextField player2Name = new TextField("Igrač 2");

        playersGrid.add(new Label("Ime igrača 1:"), 0, 0);
        playersGrid.add(player1Name, 1, 0);
        playersGrid.add(new Label("Ime igrača 2:"), 0, 1);
        playersGrid.add(player2Name, 1, 1);

        Label kugliceLabel = new Label("Odaberi broj crvenih kuglica:");
        kugliceLabel.setTextFill(Color.WHITE);

        ToggleGroup kugliceGroup = new ToggleGroup();
        RadioButton r5 = new RadioButton("5");
        r5.setTextFill(Color.WHITE);
        r5.setToggleGroup(kugliceGroup);

        RadioButton r10 = new RadioButton("10");
        r10.setTextFill(Color.WHITE);
        r10.setToggleGroup(kugliceGroup);

        RadioButton r15 = new RadioButton("15");
        r15.setTextFill(Color.WHITE);
        r15.setToggleGroup(kugliceGroup);
        r15.setSelected(true);

        HBox radioButtonsBox = new HBox(15, r5, r10, r15);
        radioButtonsBox.setAlignment(Pos.CENTER);

        kugliceGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == r5) brojCrvenih = 5;
            else if (newVal == r10) brojCrvenih = 10;
            else brojCrvenih = 15;
        });

        Button startBtn = new Button("Započni igru");
        startBtn.setOnAction(e -> {
            if (player1Name.getText().isBlank() || player2Name.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Molimo unesite imena oba igrača.").showAndWait();
                return;
            }
            stage.setScene(new GameScene(stage, player1Name.getText(), player2Name.getText(), brojCrvenih).getScene());
        });

        Button historyBtn = new Button("Istorija Mečeva");
        historyBtn.setOnAction(e -> stage.setScene(new MatchHistoryScene(stage).getScene()));

        Button reportBtn = new Button("Prijavi Grešku");
        reportBtn.setOnAction(e -> stage.setScene(new ReportScene(stage).getScene()));

        HBox topButtonBox = new HBox(20, startBtn, historyBtn);
        topButtonBox.setAlignment(Pos.CENTER);

        HBox bottomButtonBox = new HBox(20, reportBtn);
        bottomButtonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(title, playersGrid, kugliceLabel, radioButtonsBox, topButtonBox, bottomButtonBox);
        this.scene = new Scene(layout, 800, 600);
    }

    /**
     * Returns the scene for the menu screen.
     *
     * @return The constructed menu scene.
     */
    public Scene getScene() {
        return scene;
    }
}
