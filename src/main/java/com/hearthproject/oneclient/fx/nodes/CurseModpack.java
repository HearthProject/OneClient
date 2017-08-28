package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CurseModpack extends CurseTile {

	public CurseModpack(CurseElement pack) {
		super(pack);
	}

	@Override
	public void install() {
		try {
			InstallingController.showInstaller();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InstallingController.controller.setTitleText("Installing...");
		InstallingController.controller.setDetailText("Preparing to install");

		new Thread(() -> {
			Instance instance = new Instance(element.getTitle());
			instance.icon = "icon.png";
			try {
				new CursePackInstaller().downloadFromURL(element.getUrl(), "latest", instance);
				final File imageFile = getImageFile();
				if (imageFile != null)
					Files.copy(imageFile.toPath(), new File(instance.getDirectory(), "icon.png").toPath());
				MinecraftUtil.installMinecraft(instance);
			} catch (Throwable throwable) {
				OneClientLogging.error(throwable);
			}
			Platform.runLater(() -> {
				InstanceManager.addInstance(instance);
				if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
					Main.mainController.currentContent.refresh();
				}
				InstallingController.close();
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Pack has been installed!");
				alert.setHeaderText(null);
				alert.setContentText(instance.name + " has been downloaded and installed! You can find it under the instances tab.");
				alert.showAndWait();
			});

		}).start();
	}
}
