package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.minecraft.MinecraftAuth;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;

public class InstancesPane extends ContentPane {
	public TilePane instancePane;
	public ScrollPane scrollPane;

	public ArrayList<InstanceTile> instanceTiles = new ArrayList<>();
	private StackPane newInstanceTile;

	public InstancesPane() {
		super("gui/contentpanes/instances.fxml", "Home", "home.png");
	}

	@Override
	public void onStart() {
		if (newInstanceTile == null) {
			newInstanceTile = (StackPane) instancePane.getChildren().get(0);
		}
		scrollPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		scrollPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		scrollPane.setContent(instancePane);
		instancePane.prefWidthProperty().bind(scrollPane.widthProperty());
	}

	@Override
	public void refresh() {
		refreshInstances();
	}

	@SuppressWarnings("unused")
	public void onNewInstancePress() {
		NewInstanceController.start(new Instance());
	}

	private void refreshInstances() {
		if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
			InstanceManager.load();
			instancePane.getChildren().clear();
			for (Instance instance : InstanceManager.getInstances()) {
				InstanceTile tile = new InstanceTile(instance);
				instanceTiles.add(tile);
				instancePane.getChildren().add(tile);
			}
			instancePane.getChildren().add(newInstanceTile);

			for (InstanceTile tile : instanceTiles) {
				tile.setAction(() -> {
					Instance instance = InstanceManager.getInstance(tile.nameLabel.getText());
					MinecraftAuth.loginAndPlay(instance);
				});
			}
		}
	}

}
