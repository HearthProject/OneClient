package com.hearthproject.oneclient.fx.contentpane;

import com.google.gson.JsonObject;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.multimc.MMCImporter;
import com.hearthproject.oneclient.api.twitch.TwitchImporter;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.jfoenix.controls.JFXButton;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class PackPane extends ContentPane {

	public JFXButton buttonCustom, buttonCurse, buttonMMC, buttonTwitch;

	public PackPane() {
		super("gui/contentpanes/packs.fxml", "Get Modpacks", "modpacks.png", ButtonDisplay.TOP);
	}

	@Override
	protected void onStart() {
		buttonCustom.setOnAction(event -> NewInstanceController.start(null));

		buttonCurse.setOnAction(event -> {
			Main.mainController.currentContent.button.setSelected(false);
			Main.mainController.setContent(ContentPanes.CURSE_META_PANE);
		});

		buttonMMC.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip"));
			File file = chooser.showOpenDialog(null);
			if (file != null && FilenameUtils.isExtension(file.toString(), ".zip")) {
				Instance instance = new MMCImporter(file).create();
				if (instance != null) {
					instance.install();
				}
			}
		});

		buttonTwitch.setOnAction(this::openTwitch);

	}

	private void openTwitch(Event event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File directory = chooser.showDialog(null);

		if (OperatingSystem.isWindows()) {
			File twitch = new File(System.getenv("APPDATA"), "Twitch/");
			JsonObject settings = JsonUtil.read(new File(twitch, "Minecraft.settings"), JsonObject.class);
			File instances = new File(settings.get("InstanceRoot").getAsString());
			chooser.setInitialDirectory(instances);
		}
		if (directory != null) {
			findTwitch(directory);
		}
	}

	private void findTwitch(File directory) {
		if (!new File(directory, "minecraftinstance.json").exists()) {
			new Alert(Alert.AlertType.ERROR, "Not a valid Twitch Instance, the directory must have a minecraftinstance.json file ", ButtonType.OK).showAndWait();
			findTwitch(directory.getParentFile());
		}
		Instance instance = new TwitchImporter(directory).create();
		if (instance != null) {
			instance.install();
		}
	}

	@Override
	public void refresh() {

	}
}
