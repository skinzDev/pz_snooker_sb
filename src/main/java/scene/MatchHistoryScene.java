package scene;

import data.MatchData;
import database.DatabaseManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Scena za prikaz istorije odigranih mečeva u tabeli.
 * Omogućava pregled i brisanje prethodnih rezultata.
 */
public class MatchHistoryScene {

    private final Scene scene;
    private final TableView<MatchData> table = new TableView<>();
    private final Stage stage;

    public MatchHistoryScene(Stage stage) {
        this.stage = stage;

        // Initialize database if not already done
        try {
            DatabaseManager.INSTANCE.initialize();
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #016300;");
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Istorija Mečeva");
        title.setFont(Font.font("Arial", 24));
        title.setStyle("-fx-text-fill: white;");

        setupTable();
        loadData();

        Button refreshButton = new Button("Osveži");
        refreshButton.setOnAction(e -> loadData());

        Button newGameButton = new Button("Nova Igra");
        newGameButton.setOnAction(e -> stage.setScene(new MenuScene(stage).getScene()));

        Button logoutButton = new Button("Odjavi se");
        logoutButton.setOnAction(e -> stage.setScene(new LoginScene(stage).getScene()));

        HBox buttonBox = new HBox(20, refreshButton, newGameButton, logoutButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(title, table, buttonBox);
        this.scene = new Scene(layout, 800, 600);
    }

    private void setupTable() {
        // Use custom cell value factories instead of PropertyValueFactory
        TableColumn<MatchData, String> player1 = new TableColumn<>("Igrač 1");
        player1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPlayer1()));
        player1.setPrefWidth(120);
        player1.setMinWidth(100);

        TableColumn<MatchData, String> player2 = new TableColumn<>("Igrač 2");
        player2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPlayer2()));
        player2.setPrefWidth(120);
        player2.setMinWidth(100);

        TableColumn<MatchData, String> score = new TableColumn<>("Rezultat");
        score.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScore()));
        score.setPrefWidth(100);
        score.setMinWidth(80);

        TableColumn<MatchData, Integer> highestBreak = new TableColumn<>("Najveći Brejk");
        highestBreak.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getHighestBreak()).asObject());
        highestBreak.setPrefWidth(120);
        highestBreak.setMinWidth(100);

        TableColumn<MatchData, LocalDate> date = new TableColumn<>("Datum");
        date.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        date.setPrefWidth(120);
        date.setMinWidth(100);

        TableColumn<MatchData, Void> deleteCol = new TableColumn<>("Akcija");
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Obriši");

            {
                deleteButton.setOnAction(event -> {
                    MatchData match = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.initOwner(stage);
                    alert.setTitle("Potvrda brisanja");
                    alert.setHeaderText("Da li ste sigurni da želite da obrišete ovaj meč?");
                    alert.setContentText("Meč između " + match.getPlayer1() + " i " + match.getPlayer2());

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        DatabaseManager.INSTANCE.deleteMatch(match.getId());
                        loadData(); // Ponovo učitaj podatke da se osveži tabela
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        deleteCol.setPrefWidth(100);
        deleteCol.setMinWidth(80);

        table.getColumns().setAll(player1, player2, score, highestBreak, date, deleteCol);

        // Improved table styling
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setRowFactory(tv -> {
            TableRow<MatchData> row = new TableRow<MatchData>() {
                @Override
                protected void updateItem(MatchData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        // Normal row styling
                        setStyle("-fx-background-color: white; -fx-text-fill: black;");
                    }
                }
            };
            return row;
        });

        // Set placeholder for empty table
        table.setPlaceholder(new Label("Nema podataka o mečevima"));

        // Set table styling with proper selection colors
        table.setStyle(
                "-fx-background-color: white; " +
                        "-fx-selection-bar: #3498db; " +
                        "-fx-selection-bar-non-focused: #bdc3c7; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent;"
        );

        // Add CSS for better selection and hover visibility
        table.getStylesheets().add("data:text/css," +
                ".table-view { " +
                "    -fx-background-color: white; " +
                "} " +
                ".table-view .table-row-cell { " +
                "    -fx-background-color: white; " +
                "    -fx-text-fill: black; " +
                "} " +
                ".table-view .table-row-cell:hover { " +
                "    -fx-background-color: #ecf0f1 !important; " +
                "    -fx-text-fill: black !important; " +
                "} " +
                ".table-view .table-row-cell:selected { " +
                "    -fx-background-color: #3498db !important; " +
                "    -fx-text-fill: white !important; " +
                "} " +
                ".table-view .table-row-cell:selected:hover { " +
                "    -fx-background-color: #2980b9 !important; " +
                "    -fx-text-fill: white !important; " +
                "} " +
                ".table-view .table-cell { " +
                "    -fx-text-fill: inherit; " +
                "    -fx-border-color: #bdc3c7; " +
                "} " +
                ".table-view .table-row-cell:empty { " +
                "    -fx-background-color: white; " +
                "}");
    }

    private void loadData() {
        // Create a background task for database operation
        Task<List<MatchData>> loadTask = new Task<List<MatchData>>() {
            @Override
            protected List<MatchData> call() throws Exception {
                System.out.println("DEBUG: Loading data from database...");
                return DatabaseManager.INSTANCE.getAllMatches();
            }

            @Override
            protected void succeeded() {
                List<MatchData> matches = getValue();
                System.out.println("DEBUG: Successfully loaded " + matches.size() + " matches");

                // Debug: Print all match data
                for (int i = 0; i < matches.size(); i++) {
                    MatchData match = matches.get(i);
                    System.out.println("DEBUG: Match " + i + " - ID: " + match.getId() +
                            ", Player1: '" + match.getPlayer1() + "'" +
                            ", Player2: '" + match.getPlayer2() + "'" +
                            ", Score: '" + match.getScore() + "'" +
                            ", Break: " + match.getHighestBreak() +
                            ", Date: " + match.getDate());
                }

                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    table.getItems().clear();

                    if (matches.isEmpty()) {
                        System.out.println("DEBUG: No matches to display");
                    } else {
                        table.setItems(FXCollections.observableArrayList(matches));
                        System.out.println("DEBUG: Table items set, count: " + table.getItems().size());
                    }

                    table.refresh();
                    System.out.println("DEBUG: Table refreshed");
                });
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                System.err.println("ERROR: Failed to load data: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Greška");
                    alert.setHeaderText("Greška pri učitavanju podataka");
                    alert.setContentText("Detalji: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        };

        // Run the task in a background thread
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    public Scene getScene() {
        return scene;
    }
}