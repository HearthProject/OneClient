package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.HearthInstance;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.json.JsonUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InstanceManager {

	protected static Map<String, HearthInstance> instances = new HashMap<>();

	public static Collection<HearthInstance> getInstances() {
		return instances.values();
	}

	public static HearthInstance getInstance(String name) {
		return instances.get(name);
	}

	public static void addInstance(HearthInstance instance) {
		//TODO check its unique
		instances.put(instance.getName(), instance);
		save();
		init(instance);
	}

	public static void save() {
		instances.values().forEach(HearthInstance::save);
	}

	public static void init(HearthInstance instance) {
		File instanceDir = instance.getDirectory();
		FileUtil.createDirectory(instanceDir);
		for (String dir : Constants.INITIALIZE_DIRS) {
			FileUtil.findDirectory(instanceDir, dir);
		}
	}

	public static void load() {
		SplashScreen.updateProgess("Loading instances", 10);
		instances.clear();
		Arrays.stream(Constants.INSTANCEDIR.listFiles()).filter(File::isDirectory).forEach(dir -> {
			HearthInstance instance = load(dir);
			addInstance(instance);
		});
	}

	public static void setInstanceInstalling(HearthInstance instance, boolean installing) {
		//		MiscUtil.runLaterIfNeeded(() -> {
		//			ContentPanes.INSTANCES_PANE.refresh();
		//			for (InstanceTile tile : ContentPanes.INSTANCES_PANE.instanceTiles) {
		//				if (tile.instance.getManifest().getName().equals(instance.getManifest().getName())) {
		//					tile.setInstalling(installing);
		//				}
		//			}
		//		});
	}

	public static HearthInstance load(File dir) {
		return JsonUtil.read(dir, HearthInstance.class);
	}
}
