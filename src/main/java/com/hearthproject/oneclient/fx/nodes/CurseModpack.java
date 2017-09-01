package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;

public class CurseModpack extends CurseTile {

	public CurseModpack(CurseElement pack) {
		super(pack);
	}

	@Override
	public void install() {
		NotifyUtil.setText("Installing %s", element.getTitle());

		new Thread(() -> {
			Instance instance = new Instance(element.getTitle());
			instance.icon = "images/icon.png";
			try {
				new CursePackInstaller().downloadFromURL(element.getUrl(), "latest", instance);
				File image = element.getIcon();
				if (image != null)
					Files.copy(image.toPath(), new File(instance.getDirectory(), "images/icon.png").toPath());
				MinecraftUtil.installMinecraft(instance);
			} catch (Throwable throwable) {
				OneClientLogging.error(throwable);
			}
			Platform.runLater(() -> {
				InstanceManager.addInstance(instance);
				if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
					Main.mainController.currentContent.refresh();
				}
				NotifyUtil.setText(Duration.seconds(10), "%s has been downloaded and installed!", instance.name);
			});

		}).start();
	}
}
