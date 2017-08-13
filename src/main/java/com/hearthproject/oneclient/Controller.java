package com.hearthproject.oneclient;

import javafx.fxml.FXML;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.awt.*;

public class Controller {
    @FXML
    public Rectangle tabBar;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public TilePane instancePane;

    public void onStart(Stage stage) {
        tabBar.setWidth(Integer.MAX_VALUE);
    }
}
