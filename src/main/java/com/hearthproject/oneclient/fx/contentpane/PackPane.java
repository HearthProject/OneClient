package com.hearthproject.oneclient.fx.contentpane;

import com.google.gson.JsonObject;
import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.modpack.DownloadManager;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.curse.CurseZipImporter;
import com.hearthproject.oneclient.api.modpack.multimc.MMCImporter;
import com.hearthproject.oneclient.api.modpack.twitch.TwitchImporter;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.jfoenix.controls.JFXButton;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class PackPane extends ContentPane {

	public JFXButton buttonCustom, buttonHearth, buttonCurse, buttonMMC, buttonTwitch, buttonCurseZIP;

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
			if (file != null && FilenameUtils.isExtension(file.toString(), "zip")) {
				new Thread(() -> {
					Instance instance = new MMCImporter(file).create();
					if (instance != null) {
						instance.install();
					}
				}).start();
				Main.mainController.setContent(ContentPanes.INSTANCES_PANE);
			}
		});

		buttonTwitch.setOnAction(this::openTwitch);

		buttonHearth.setOnAction(event -> Main.mainController.setContent(ContentPanes.PRIVATE_PACK_PANE));

		buttonCurseZIP.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip"));
			chooser.setTitle("Curse ZIP Importer");
			File file = chooser.showOpenDialog(null);
			if (file != null && FilenameUtils.isExtension(file.toString(), "zip")) {
				Instance instance = new CurseZipImporter(file).create();
				if (instance != null) {
					DownloadTask task = DownloadManager.createDownload(instance.getName(), instance::install);
					task.start();
				}
				Main.mainController.setContent(ContentPanes.INSTANCES_PANE);
			}
		});
	}

	private void openTwitch(Event event) {
		DirectoryChooser chooser = new DirectoryChooser();

		if (OperatingSystem.isWindows()) {
			File twitch = new File(System.getenv("APPDATA"), "Twitch/");
			JsonObject settings = JsonUtil.read(new File(twitch, "Minecraft.settings"), JsonObject.class);
			File instances = new File(settings.get("InstanceRoot").getAsString());
			OneClientLogging.info("{}", instances);
			chooser.setInitialDirectory(instances);
		}
		File directory = chooser.showDialog(null);
		if (directory != null) {
			findTwitch(directory);
		}
	}

	private void findTwitch(File directory) {
		if (!new File(directory, "minecraftinstance.json").exists()) {
			new Alert(Alert.AlertType.ERROR, "Not a valid Twitch Instance, the directory must have a minecraftinstance.json file ", ButtonType.OK).showAndWait();
			findTwitch(directory.getParentFile());
		}
		new Thread(() -> {
			Instance instance = new TwitchImporter(directory).create();
			if (instance != null) {
				instance.install();
			}
		}).start();
		Main.mainController.setContent(ContentPanes.INSTANCES_PANE);
	}

	@Override
	public void refresh() {

	}
}
