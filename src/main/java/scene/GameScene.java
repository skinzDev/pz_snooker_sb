package scene;

import database.DatabaseManager;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import logika.Snooker;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Snooker Game Scene
 * <p>
 * This class manages and displays the main game screen for a snooker match.
 * It handles the UI for the game board, score display, player turns,
 * and all user interactions, linking the visual elements to the underlying game logic.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.1
 */
public class GameScene {
    private final Scene scene;
    private final Snooker snooker;
    private final String player1Name;
    private final String player2Name;

    private final Label scoreLabel = new Label();
    private final Label playerTurnLabel = new Label();
    private final Label infoLabel = new Label();
    private final Label breakLabel = new Label();
    private final Map<Integer, Button> ballButtons = new HashMap<>();

    /**
     * Constructs the game scene.
     *
     * @param stage       The primary stage of the application.
     * @param p1Name      The name of player 1.
     * @param p2Name      The name of player 2.
     * @param brojCrvenih The number of red balls to start the game with.
     */
    public GameScene(Stage stage, String p1Name, String p2Name, int brojCrvenih) {
        this.snooker = new Snooker(brojCrvenih);
        this.player1Name = p1Name;
        this.player2Name = p2Name;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #016300; -fx-border-color: #3B2A1A; -fx-border-width: 20;");
        root.setPadding(new Insets(20));

        breakLabel.setFont(Font.font("Arial", 16));
        breakLabel.setTextFill(Color.AQUA);
        VBox infoBox = new VBox(10, scoreLabel, playerTurnLabel, infoLabel, breakLabel);
        infoBox.setAlignment(Pos.CENTER);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabel.setTextFill(Color.WHITE);
        playerTurnLabel.setFont(Font.font("Arial", 18));
        playerTurnLabel.setTextFill(Color.WHITE);
        infoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        infoLabel.setTextFill(Color.YELLOW);
        root.setTop(infoBox);

        GridPane buttonsPane = createBallsGrid(stage);
        root.setCenter(buttonsPane);

        Button endTurnBtn = new Button("Završi potez");
        endTurnBtn.setOnAction(e -> {
            snooker.promasaj();
            updateDisplay();
        });

        Button foulBtn = new Button("Foul +4");
        foulBtn.setOnAction(e -> {
            snooker.foulPlusFour();
            updateDisplay();
        });

        HBox controlBox = new HBox(20, endTurnBtn, foulBtn);
        controlBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(controlBox, new Insets(15, 0, 0, 0));
        root.setBottom(controlBox);

        this.scene = new Scene(root, 900, 700);
        updateDisplay();
    }

    /**
     * Creates the grid of buttons representing the snooker balls.
     * Each button is associated with a ball value and has an image.
     *
     * @param stage The primary stage, used for handling clicks.
     * @return A GridPane containing the ball buttons.
     */
    private GridPane createBallsGrid(Stage stage) {
        GridPane buttonsPane = new GridPane();
        buttonsPane.setHgap(15);
        buttonsPane.setVgap(15);
        buttonsPane.setAlignment(Pos.CENTER);

        int[] points = {1, 2, 3, 4, 5, 6, 7};
        String[] imagePaths = {"/images/crvena.png", "/images/zuta.png", "/images/zelena.png", "/images/braon.png", "/images/plava.png", "/images/roze.png", "/images/crna.png"};

        for (int i = 0; i < points.length; i++) {
            final int value = points[i];
            try {
                Image img = new Image(Objects.requireNonNull(getClass().getResource(imagePaths[i])).toExternalForm());
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                Button b = new Button("", imageView);
                b.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                b.setOnAction(e -> handleBallClick(value, stage));
                ballButtons.put(value, b);
                buttonsPane.add(b, i, 0);
            } catch (Exception ex) {
                System.err.println("Error: Image not found - " + imagePaths[i]);
                Button b = new Button(String.valueOf(value));
                b.setPrefSize(80, 80);
                b.setOnAction(e -> handleBallClick(value, stage));
                ballButtons.put(value, b);
                buttonsPane.add(b, i, 0);
            }
        }
        return buttonsPane;
    }

    /**
     * Handles the logic when a ball button is clicked.
     * It updates the game state and checks if the game is over.
     *
     * @param value The point value of the clicked ball.
     * @param stage The primary stage, to switch scenes when the game ends.
     */
    private void handleBallClick(int value, Stage stage) {
        snooker.klikNaBoju(value);
        updateDisplay();
        if (snooker.isGameOver()) {
            showWinnerAndSave(stage);
        }
    }

    /**
     * Handles the end-of-game sequence. It saves the match result and any high breaks
     * to the database, displays a winner announcement, and navigates to the match history scene.
     *
     * @param stage The primary stage, used to show alerts and switch scenes.
     */
    private void showWinnerAndSave(Stage stage) {
        int matchId = DatabaseManager.INSTANCE.saveMatchResult(player1Name, player2Name, snooker.getPoeni1(), snooker.getPoeni2());

        if (matchId == -1) {
            new Alert(Alert.AlertType.ERROR, "Error connecting to the database. The result was not saved.").showAndWait();
        } else {
            int highestBreak = snooker.getHighestBreakInMatch();
            if (highestBreak > 0) {
                int playerNum = snooker.getPlayerWithHighestBreak();
                String breakPlayerName = (playerNum == 1) ? player1Name : player2Name;
                DatabaseManager.INSTANCE.saveHighestBreak(matchId, breakPlayerName, highestBreak);
            }
        }

        String winner;
        if (snooker.getPoeni1() > snooker.getPoeni2()) winner = player1Name;
        else if (snooker.getPoeni2() > snooker.getPoeni1()) winner = player2Name;
        else winner = "Nerešeno";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kraj igre!");
        alert.setHeaderText("Pobednik je: " + winner);
        alert.setContentText(String.format("Konačan rezultat: %d : %d\nNajveći brejk na meču: %d",
                snooker.getPoeni1(), snooker.getPoeni2(), snooker.getHighestBreakInMatch()));
        alert.showAndWait();

        stage.setScene(new MatchHistoryScene(stage).getScene());
    }

    /**
     * Updates all UI labels and button states to reflect the current game state.
     * This includes scores, player turn, break information, and which balls are currently playable.
     */
    private void updateDisplay() {
        scoreLabel.setText(String.format("%s %d : %d %s", player1Name, snooker.getPoeni1(), snooker.getPoeni2(), player2Name));
        playerTurnLabel.setText("Na potezu: " + (snooker.jeIgrac1NaRedu() ? player1Name : player2Name));
        breakLabel.setText(String.format("Trenutni brejk: %d  |  Najveći brejk: %d", snooker.getCurrentBreak(), snooker.getHighestBreakInMatch()));

        if (snooker.isEndgame()) {
            infoLabel.setText("ENDGAME! Na redu je " + getColorName(snooker.getNextColorValue()));
            for (Map.Entry<Integer, Button> entry : ballButtons.entrySet()) {
                entry.getValue().setDisable(entry.getKey() != snooker.getNextColorValue());
            }
        } else {
            boolean naReduCrvena = snooker.daLiTrebaCrvena();
            infoLabel.setText(naReduCrvena ? "Na redu je CRVENA kugla (" + snooker.getCrvenePreostale() + " preostalo)" : "Na redu je OBOJENA kugla");
            for (Map.Entry<Integer, Button> entry : ballButtons.entrySet()) {
                int buttonValue = entry.getKey();
                Button b = entry.getValue();
                b.setDisable(naReduCrvena ? (buttonValue != 1) : (buttonValue == 1));
            }
        }
    }

    /**
     * Converts a ball's point value to its color name.
     *
     * @param value The point value of the ball.
     * @return The name of the color as a string.
     */
    private String getColorName(int value) {
        return Map.of(2, "ŽUTA", 3, "ZELENA", 4, "BRAON", 5, "PLAVA", 6, "ROZE", 7, "CRNA").getOrDefault(value, "");
    }

    /**
     * Returns the scene for the game screen.
     *
     * @return The constructed game scene.
     */
    public Scene getScene() { return scene; }
}