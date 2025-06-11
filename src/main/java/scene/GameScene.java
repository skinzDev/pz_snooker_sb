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
 * Scene that displays the course of a Snooker match.
 * Contains GUI components for interaction and displaying the game state.
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

    public GameScene(Stage stage, String p1Name, String p2Name, int brojCrvenih) {
        this.snooker = new Snooker(brojCrvenih);
        this.player1Name = p1Name;
        this.player2Name = p2Name;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #016300; -fx-border-color: #3B2A1A; -fx-border-width: 20;");
        root.setPadding(new Insets(20));

        // Info section at the top
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

        // Ball buttons in the center
        GridPane buttonsPane = createBallsGrid(stage);
        root.setCenter(buttonsPane);

        // Control buttons at the bottom
        Button endTurnBtn = new Button("Završi potez / Promašaj");
        endTurnBtn.setOnAction(e -> {
            snooker.promasaj();
            updateDisplay();
        });
        HBox controlBox = new HBox(20, endTurnBtn);
        controlBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(controlBox, new Insets(15, 0, 0, 0));
        root.setBottom(controlBox);

        this.scene = new Scene(root, 900, 700);
        updateDisplay();
    }

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
                Button b = new Button(String.valueOf(value)); // Fallback
                b.setPrefSize(80, 80);
                b.setOnAction(e -> handleBallClick(value, stage));
                ballButtons.put(value, b);
                buttonsPane.add(b, i, 0);
            }
        }
        return buttonsPane;
    }

    private void handleBallClick(int value, Stage stage) {
        snooker.klikNaBoju(value);
        updateDisplay();
        if (snooker.isGameOver()) {
            showWinnerAndSave(stage);
        }
    }

    private void showWinnerAndSave(Stage stage) {
        // Step 1: Save match result and get the new match_id
        int matchId = DatabaseManager.INSTANCE.saveMatchResult(player1Name, player2Name, snooker.getPoeni1(), snooker.getPoeni2());

        if (matchId == -1) {
            new Alert(Alert.AlertType.ERROR, "Error connecting to the database. The result was not saved.").showAndWait();
        } else {
            // Step 2: If there was a break, save it to the new breaks table
            int highestBreak = snooker.getHighestBreakInMatch();
            if (highestBreak > 0) {
                int playerNum = snooker.getPlayerWithHighestBreak();
                String breakPlayerName = (playerNum == 1) ? player1Name : player2Name;
                DatabaseManager.INSTANCE.saveHighestBreak(matchId, breakPlayerName, highestBreak);
            }
        }

        // Step 3: Announce the winner
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

        // Step 4: Go to the match history scene
        stage.setScene(new MatchHistoryScene(stage).getScene());
    }

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

    private String getColorName(int value) {
        return Map.of(2, "ŽUTA", 3, "ZELENA", 4, "BRAON", 5, "PLAVA", 6, "ROZE", 7, "CRNA").getOrDefault(value, "");
    }

    public Scene getScene() { return scene; }
}
