package scene;

import data.TaCScrapper;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TermsAndConditionsApplication {
    private Stage stage;

    private TaCScrapper scrapper = new TaCScrapper();

    public void launch(){
        displayUI();
    }

    private void displayUI()
    {
        stage = new Stage();
        TextArea ta = new TextArea("Loading Terms and Conditions...");
        ta.setWrapText(true);
        ta.setEditable(false);
        StackPane root = new StackPane(ta);

        new Thread(() -> {
            scrapper.run();
            String result = scrapper.getResult();

            Platform.runLater(() -> ta.setText(result));
        }).start();

        Scene scene = new Scene(root, 800, 640);

        stage.setScene(scene);
        stage.setTitle("Terms and Conditions - Snooker Score Tracker ");
        stage.show();
    }
}
