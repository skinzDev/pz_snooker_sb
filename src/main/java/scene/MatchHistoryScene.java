package scene;

import data.MatchData;
import database.DatabaseManager;
import javafx.collections.FXCollections;
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
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #016300;");
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Istorija Mečeva");
        title.setFont(Font.font("Arial", 24));
        title.setStyle("-fx-text-fill: white;");

        setupTable();
        loadData();

        Button newGameButton = new Button("Nova Igra");
        newGameButton.setOnAction(e -> stage.setScene(new MenuScene(stage).getScene()));

        Button logoutButton = new Button("Odjavi se");
        logoutButton.setOnAction(e -> stage.setScene(new LoginScene(stage).getScene()));

        HBox buttonBox = new HBox(20, newGameButton, logoutButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(title, table, buttonBox);
        this.scene = new Scene(layout, 800, 600);
    }

    private void setupTable() {
        TableColumn<MatchData, String> player1Col = new TableColumn<>("Igrač 1");
        player1Col.setCellValueFactory(new PropertyValueFactory<>("player1"));

        TableColumn<MatchData, String> player2Col = new TableColumn<>("Igrač 2");
        player2Col.setCellValueFactory(new PropertyValueFactory<>("player2"));

        TableColumn<MatchData, String> scoreCol = new TableColumn<>("Rezultat");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<MatchData, Integer> breakCol = new TableColumn<>("Najveći Brejk");
        breakCol.setCellValueFactory(new PropertyValueFactory<>("highestBreak"));

        TableColumn<MatchData, LocalDate> dateCol = new TableColumn<>("Datum");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

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

        table.getColumns().setAll(player1Col, player2Col, scoreCol, breakCol, dateCol, deleteCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadData() {
        List<MatchData> matches = DatabaseManager.INSTANCE.getAllMatches();
        table.setItems(FXCollections.observableArrayList(matches));
    }

    public Scene getScene() {
        return scene;
    }
}