package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InstanceManager {

	protected static Map<String, Instance> instances = new HashMap<>();

	public static Collection<Instance> getInstances() {
		return instances.values();
	}

	public static Instance getInstance(String name) {
		return instances.get(name);
	}

	public static void addInstance(Instance instance) {
		//TODO check its unique
		instances.put(instance.name, instance);
		save();
		init(instance);
	}

	public static boolean isValid(String name) {
		return isValid(new Instance(name));
	}

	public static boolean isValid(Instance instance) {
		File dir = instance.getDirectory();
		if (dir.exists()) {
			return false;
		}
		return true;
	}

	public static void save() {
		instances.values().forEach(InstanceManager::save);
	}

	public static void save(Instance instance) {
		File dir = instance.getDirectory();
		File jsonFile = new File(dir, "instance.json");
		String jsonStr = JsonUtil.GSON.toJson(instance);
		try {
			org.apache.commons.io.FileUtils.writeStringToFile(jsonFile, jsonStr, StandardCharsets.UTF_8);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
	}

	public static void init(Instance instance) {
		File instanceDir = instance.getDirectory();
		for (String dir : Constants.INITIALIZE_DIRS) {
			FileUtil.findDirectory(instanceDir, dir);
		}
	}

	public static void load() {
		SplashScreen.updateProgess("Loading instances", 10);
		instances.clear();
		Arrays.stream(Constants.INSTANCEDIR.listFiles()).filter(File::isDirectory).forEach(dir -> {
			try {
				File jsonFile = new File(dir, "instance.json");
				if (!jsonFile.exists()) {
					OneClientLogging.logger.error("ERROR: An invalid instance with the name " + dir.getName() + " is has been found, it will be ignored.");
					return;
				}
				String jsonStr = org.apache.commons.io.FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
				Instance instance = JsonUtil.GSON.fromJson(jsonStr, Instance.class);
				instances.put(instance.name, instance);
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
		});
	}

	public static void setInstanceInstalling(Instance instance, boolean installing) {
		MiscUtil.runLaterIfNeeded(() -> {
			ContentPanes.INSTANCES_PANE.refresh();
			for (InstanceTile tile : ContentPanes.INSTANCES_PANE.instanceTiles) {
				if (tile.instance.name.equals(instance.name)) {
					tile.setInstalling(installing);
				}
			}
		});
	}

	public static void removeInstance(Instance instance) {
		save();
	}

}
