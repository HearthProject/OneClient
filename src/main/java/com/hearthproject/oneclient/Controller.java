package com.hearthproject.oneclient;

import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    public void onStart(Stage stage) throws IOException {
        tabBar.setWidth(Integer.MAX_VALUE);
        setProgress(40);
	    GameVersion gameVersion = MinecraftUtil.loadGameVersion();
	    gameVersion.versions.stream().filter(version -> version.type.equals("release")).forEach(version -> mcVersionChoiceBox.getItems().add(version.id));
	    mcVersionChoiceBox.getSelectionModel().selectFirst();

    }

    public void onNewInstancePress() {

    }

    public void onPlayPress() {
    }

    public void setProgress(double percent) {
        progressBar.setProgress(percent / 100);
    }
}
