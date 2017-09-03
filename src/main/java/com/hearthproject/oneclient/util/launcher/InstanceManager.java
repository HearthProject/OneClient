package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;

import java.io.File;
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
		instances.put(instance.getManifest().getName(), instance);
		save();
		init(instance);
	}

	public static void save() {
		instances.values().forEach(Instance::save);
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
			Instance instance = Instance.load(dir);
			if (instance != null)
				instances.put(instance.getManifest().getName(), instance);
		});
	}

	public static void setInstanceInstalling(Instance instance, boolean installing) {
		MiscUtil.runLaterIfNeeded(() -> {
			ContentPanes.INSTANCES_PANE.refresh();
			for (InstanceTile tile : ContentPanes.INSTANCES_PANE.instanceTiles) {
				if (tile.instance.getManifest().getName().equals(instance.getManifest().getName())) {
					tile.setInstalling(installing);
				}
			}
		});
	}

	public static void removeInstance(Instance instance) {
		save();
	}

}
