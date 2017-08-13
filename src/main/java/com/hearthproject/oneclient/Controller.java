package com.hearthproject.oneclient;

import com.hearthproject.oneclient.fxnodes.InstanceTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.util.launcher.InstanceUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.fxml.FXML;
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

    public ArrayList<InstanceTile> instanceTiles = new ArrayList<>();

    public void onStart(Stage stage) throws IOException {
        GameVersion gameVersion = MinecraftUtil.loadGameVersion();
        gameVersion.versions.stream().filter(version -> version.type.equals("release")).forEach(version -> mcVersionComboBox.getItems().add(version.id));
        mcVersionComboBox.getSelectionModel().selectFirst();
        refreshInstances();
    }

    public void refreshInstances() {
        StackPane newInstanceTile = (StackPane) instancePane.getChildren().get(0);
        instancePane.getChildren().clear();
        for (Instance instance : InstanceUtil.getInstances().instances) {
            InstanceTile tile = new InstanceTile(instance);
            instanceTiles.add(tile);
            instancePane.getChildren().add(tile);
        }
        instancePane.getChildren().add(newInstanceTile);

        //TODO: Remove, this is an example on how to set the instance's button action
        for (InstanceTile tile : instanceTiles) {
            tile.setAction(() -> System.out.println(tile.nameLabel.getText()));
        }
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
