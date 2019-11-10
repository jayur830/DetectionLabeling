package DetectionLabeling;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Dialog extends Application {
    public Stage stage;

    @FXML
    public Button dialogYesButton, dialogNoButton;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
    }
}
