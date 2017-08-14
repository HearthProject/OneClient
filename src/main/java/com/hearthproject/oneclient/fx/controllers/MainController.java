package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class MainController {
	@FXML
	public Rectangle tabBar;
	@FXML
	public ScrollPane scrollPane;
	@FXML
	public TilePane instancePane;
	@FXML
	public HBox barBox;
	@FXML
	public Button newInstanceButton;

	public ArrayList<InstanceTile> instanceTiles = new ArrayList<>();

	private StackPane newInstanceTile;

	public void onStart(Stage stage) throws IOException {
		newInstanceTile = (StackPane) instancePane.getChildren().get(0);
		refreshInstances();
	}

	public void refreshInstances() {
		InstanceManager.load();
		instancePane.getChildren().clear();
		for (Instance instance : InstanceManager.getInstances()) {
			InstanceTile tile = new InstanceTile(instance);
			instanceTiles.add(tile);
			instancePane.getChildren().add(tile);
		}
		instancePane.getChildren().add(newInstanceTile);

		//TODO: Remove, this is an example on how to set the instance's button action
		for (InstanceTile tile : instanceTiles) {
			tile.setAction(() -> {
				Instance instance = InstanceManager.getInstance(tile.nameLabel.getText());
				try {
					MinecraftUtil.loadMC(instance);
				} catch (Throwable e) {
					OneClientLogging.log(e);
				}
			});

		}
	}

	public void onNewInstancePress() {
		NewInstanceController.start();
	}

	public void onPlayPress() {
	}

	public void onSceneResize(Scene scene) {
		tabBar.setWidth(scene.getWidth());
	}
}
