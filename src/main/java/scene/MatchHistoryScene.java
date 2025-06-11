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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Scene for displaying the history of played matches in a table.
 * Allows viewing and deleting previous results.
 * This version uses type-safe lambda expressions for cell value factories.
 */
public class MatchHistoryScene {

    private final Scene scene;
    private final TableView<MatchData> table = new TableView<>();
    private final Stage stage;

    public MatchHistoryScene(Stage stage) {
        this.stage = stage;
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
        // Use type-safe lambda expressions instead of PropertyValueFactory
        TableColumn<MatchData, String> player1 = new TableColumn<>("Igrač 1");
        player1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPlayer1()));
        player1.setPrefWidth(150);

        TableColumn<MatchData, String> player2 = new TableColumn<>("Igrač 2");
        player2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPlayer2()));
        player2.setPrefWidth(150);

        TableColumn<MatchData, String> score = new TableColumn<>("Rezultat");
        score.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScore()));
        score.setPrefWidth(100);

        TableColumn<MatchData, LocalDate> date = new TableColumn<>("Datum");
        date.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        date.setPrefWidth(150);

        TableColumn<MatchData, Void> deleteCol = new TableColumn<>("Akcija");
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Obriši");
            {
                deleteButton.setOnAction(event -> {
                    MatchData match = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Da li ste sigurni da želite da obrišete ovaj meč?", ButtonType.OK, ButtonType.CANCEL);
                    alert.initOwner(stage);
                    alert.setTitle("Potvrda brisanja");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        DatabaseManager.INSTANCE.deleteMatch(match.getId());
                        loadData();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        table.getColumns().setAll(player1, player2, score, date, deleteCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Nema podataka o mečevima"));
    }

    private void loadData() {
        Task<List<MatchData>> loadTask = new Task<>() {
            @Override
            protected List<MatchData> call() {
                return DatabaseManager.INSTANCE.getAllMatches();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> table.setItems(FXCollections.observableArrayList(getValue())));
            }



            @Override
            protected void failed() {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Greška pri učitavanju podataka iz baze.").show());
                getException().printStackTrace();
            }
        };
        new Thread(loadTask).start();
    }

    public Scene getScene() {
        return scene;
    }
}
