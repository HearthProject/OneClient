package com.hearthproject.oneclient.fx.controllers.content;

import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;

public class InstancePaneController extends ContentPaneController {
	@FXML
	public TilePane instancePane;
	@FXML
	public Button newInstanceButton;
	private ArrayList<InstanceTile> instanceTiles = new ArrayList<>();
	private StackPane newInstanceTile;

	@Override
	public void onStart() {
		newInstanceTile = (StackPane) instancePane.getChildren().get(0);
	}

	@Override
	public void refresh() {
		refreshInstances();
	}

	public void onNewInstancePress() {
		NewInstanceController.start();
	}

	private void refreshInstances() {
		if (controller.currentContent == MainController.Content.INSTANCES) {
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
						MinecraftUtil.installMinecraft(instance);
						MinecraftUtil.startMinecraft(instance);
					} catch (Throwable e) {
						OneClientLogging.log(e);
					}
				});

			}
		}
	}
}
