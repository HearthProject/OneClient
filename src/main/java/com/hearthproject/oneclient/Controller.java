package com.hearthproject.oneclient;

import com.hearthproject.oneclient.fxnodes.InstanceTile;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

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
    public ComboBox instanceComboBox;
    @FXML
    public Button playButton;
    @FXML
    public Button newInstanceButton;
    @FXML
    public TextField instanceNameField;
    @FXML
    public ComboBox mcVersionComboBox;

    public void onStart(Stage stage) throws IOException {
        setProgress(40);
        GameVersion gameVersion = MinecraftUtil.loadGameVersion();
        gameVersion.versions.stream().filter(version -> version.type.equals("release")).forEach(version -> mcVersionComboBox.getItems().add(version.id));
        mcVersionComboBox.getSelectionModel().selectFirst();

    }

    public void onNewInstancePress() {

    }

    public void onPlayPress() {
    }

    public void onSceneResize(Scene scene) {
        tabBar.setWidth(scene.getWidth());
        progressBar.setPrefWidth(scene.getWidth() - 12);
    }

    public void setProgress(double percent) {
        progressBar.setProgress(percent / 100);
    }
}
