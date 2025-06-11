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
 * Scena koja prikazuje tok Snooker partije.
 * Sadrži GUI komponente za interakciju i prikaz stanja igre.
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

        // Info sekcija na vrhu
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

        // Dugmad za kugle u centru
        GridPane buttonsPane = createBallsGrid(stage);
        root.setCenter(buttonsPane);

        // Kontrolna dugmad na dnu
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
                System.err.println("Greška: Slika nije pronađena - " + imagePaths[i]);
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
            showWinner(stage);
        }
    }

    private void showWinner(Stage stage) {

        System.out.println("showWinner called"); // Debug line

        boolean saved = DatabaseManager.INSTANCE.saveMatchResult(player1Name, player2Name, snooker.getPoeni1(), snooker.getPoeni2(), snooker.getHighestBreakInMatch());

        System.out.println("Match result: " + saved);
        if (!saved) {
            new Alert(Alert.AlertType.ERROR, "Greška pri konekciji sa bazom. Rezultat nije sačuvan.").showAndWait();
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