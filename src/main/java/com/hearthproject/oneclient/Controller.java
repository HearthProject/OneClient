package com.hearthproject.oneclient;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
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

    public void onStart(Stage stage) {
        tabBar.setWidth(Integer.MAX_VALUE);
        setProgress(40);
    }

    public void onNewInstancePress() {

    }

    public void onPlayPress() {
    }

    public void setProgress(double percent) {
        progressBar.setProgress(Double.parseDouble("0." + String.valueOf(percent).replace(".", "")));
    }
}
