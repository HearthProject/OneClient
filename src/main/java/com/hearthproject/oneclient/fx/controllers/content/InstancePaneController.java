package com.hearthproject.oneclient.fx.controllers.content;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftAuth;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.event.ActionEvent;
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
	public Button importCurseButton;
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

			for (InstanceTile tile : instanceTiles) {
				tile.setAction(() -> {
					Instance instance = InstanceManager.getInstance(tile.nameLabel.getText());
					MinecraftAuth.loginAndPlay(instance);
				});

			}
		}
	}

	public void onCursePress(ActionEvent actionEvent) {
		Instance instance = new Instance("All the mods 3");
		instance.minecraftVersion = "1.12";
		instance.modLoaderVersion = "14.21.1.2443";
		instance.modLoader = "forge";
		InstanceManager.addInstance(instance);
		if (Main.mainController.currentContent == MainController.Content.INSTANCES) {
			Main.mainController.contentPaneController.refresh();
		}
		new Thread(() -> {
			try {
				MinecraftUtil.installMinecraft(instance);
				CursePackInstaller.downloadFromURL("https://minecraft.curseforge.com/projects/all-the-mods-3", "latest", instance);
			} catch (Throwable throwable) {
				OneClientLogging.log(throwable);
			}
		}).start();
	}
}
