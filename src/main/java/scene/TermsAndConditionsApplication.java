package scene;

import data.TaCScrapper;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Terms and Conditions Viewer
 * <p>
 * This class creates a separate window to display the application's Terms and Conditions.
 * It uses the TaCScrapper class to fetch the content from a web page in a background
 * thread and displays it in a non-editable TextArea.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class TermsAndConditionsApplication {
    private Stage stage;
    private final TaCScrapper scrapper = new TaCScrapper();

    /**
     * Launches the Terms and Conditions window.
     */
    public void launch() {
        displayUI();
    }

    /**
     * Creates and displays the UI for the Terms and Conditions window.
     * It shows a loading message while the content is being scraped from a URL
     * on a background thread.
     */
    private void displayUI() {
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
        stage.setTitle("Terms and Conditions - Snooker Score Tracker");
        stage.show();
    }
}
