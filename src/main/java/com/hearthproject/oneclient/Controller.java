package com.hearthproject.oneclient;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


public class Controller {
    @FXML
    public Rectangle tabBar;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public TilePane instancePane;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public ChoiceBox instanceChoiceBox;
    @FXML
    public Button playButton;
    @FXML
    public Button newInstanceButton;
    @FXML
    public TextField instanceNameField;
    @FXML
    public ChoiceBox mcVersionChoiceBox;

    public void onStart(Stage stage) {
        tabBar.setWidth(Integer.MAX_VALUE);
        setProgress(40);
        mcVersionChoiceBox.getItems().addAll("1.12.1", "1.12", "1.11.2");
    }

    public void onNewInstancePress() {

    }

    public void onPlayPress() {
    }

    public void setProgress(double percent) {
        progressBar.setProgress(percent / 100);
    }
}
