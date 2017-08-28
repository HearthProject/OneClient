package com.hearthproject.oneclient.util.curse;

import com.google.gson.JsonObject;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CursePackImporter {

	public static void importPacks() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(Main.stage);
		new Thread(() -> {
			if (selectedDirectory != null && selectedDirectory.exists()) {
				File instancesDir = new File(selectedDirectory, "Instances");
				if (instancesDir.exists() && instancesDir.listFiles() != null) {
					final int[] count = { 0 };
					Arrays.stream(instancesDir.listFiles()).filter(File::isDirectory).forEach(file -> {
						File minecraftinstance = new File(file, "minecraftinstance.json");
						if (minecraftinstance.exists()) {
							try {
								JsonObject jsonObject = JsonUtil.GSON.fromJson(FileUtils.readFileToString(minecraftinstance, StandardCharsets.UTF_8), JsonObject.class);
								String name = jsonObject.get("name").getAsString();
								String mcVersion = jsonObject.getAsJsonObject("baseModLoader").get("MinecraftVersion").getAsString();
								Instance instance = new Instance(name);
								int i = 0;
								while (!InstanceManager.isValid(instance)) {
									instance.name = name + "(" + i++ + ")";
								}
								instance.minecraftVersion = mcVersion;
								instance.modLoader = "Forge";
								instance.modLoaderVersion = jsonObject.getAsJsonObject("baseModLoader").get("Name").getAsString().replace("forge-", "");
								instance.getDirectory().mkdirs();
								FileUtils.copyDirectory(file, instance.getDirectory());
								MinecraftUtil.installMinecraft(instance);
								InstanceManager.addInstance(instance);
								OneClientLogging.logger.info("Import of " + file.getName() + " was successful!");
							} catch (Throwable e) {
								OneClientLogging.logger.error(e);
								OneClientLogging.logger.info("Import of " + file.getName() + " failed!");
							}
						}
					});
				} else {
					OneClientLogging.logUserError(new FileNotFoundException("Invalid curse install directory"), "Invalid curse install directory");
				}
			} else {
				return;
			}
			OneClientLogging.logger.info("Done!");
			MiscUtil.runLaterIfNeeded(() -> {
				ContentPanes.INSTANCES_PANE.refresh();
				ContentPanes.INSTANCES_PANE.getButton().fire();
			});
		}).start();
	}
}
