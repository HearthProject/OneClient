package com.hearthproject.oneclient.api;

import com.google.gson.JsonObject;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class InstanceManager {

	public static AtomicBoolean installing = new AtomicBoolean(false);

	protected static Map<String, Instance> instances = new HashMap<>();

	public static Collection<Instance> getInstances() {
		return instances.values();
	}

	public static Instance getInstance(String name) {
		return instances.get(name);
	}

	public static void addInstance(Instance instance) {
		//TODO check its unique
		for (Instance i : instances.values()) {
			int j = 2;
			while (instance.getName().equals(i.getName())) {
				instance.setName(instance.getName() + "i" + j);
			}
		}
		instances.put(instance.getName(), instance);
		save();
	}

	public static void save() {
		instances.values().forEach(Instance::save);
	}

	public static void load() {
		OneClientLogging.logger.info("Loading Instances");
		SplashScreen.updateProgess("Loading instances", 10);
		instances.clear();
		File[] dirs = Constants.INSTANCEDIR.listFiles(File::isDirectory);
		if (dirs != null) {
			Arrays.stream(dirs).filter(File::isDirectory).forEach(dir -> {
				Instance instance = load(dir);
				if (instance != null) {
					instances.put(instance.getName(), instance);
					instance.verifyMods();
				}
			});
		}
	}

	@Deprecated
	private static Instance loadLegacy(File dir) {
		//Loads old instances
		File manifestJson = new File(dir, "manifest.json");
		if (manifestJson.exists()) {
			try {
				JsonObject jsonObject = JsonUtil.read(manifestJson, JsonObject.class);
				Instance newInstance = new Instance();
				newInstance.name = jsonObject.get("name").getAsString();
				newInstance.icon = jsonObject.get("icon").getAsString();
				newInstance.gameVersion = jsonObject.get("minecraft").getAsJsonObject().get("version").getAsString();
				String forgeVersion = jsonObject.get("minecraft").getAsJsonObject().get("modLoaders").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
				newInstance.forgeVersion = forgeVersion.split("-")[1];
				return newInstance;
			} catch (Exception e) {
				//TODO let the user know it could not be imported?
			}
		}
		return null;
	}

	private static Instance load(File dir) {
		File instanceJson = new File(dir, "instance.json");
		Instance instance = JsonUtil.read(instanceJson, Instance.class);
		if (instance == null) {
			instance = loadLegacy(dir);
		}
		return instance;
	}

	public static void setInstanceInstalling(Instance instance, boolean installing) {
		MiscUtil.runLaterIfNeeded(() -> {
			ContentPanes.INSTANCES_PANE.refresh();
			for (InstanceTile tile : ContentPanes.INSTANCES_PANE.instanceTiles) {
				if (tile.instance.getName().equals(instance.getName())) {
					tile.setInstalling(installing);
				}
			}
		});
	}

	public static void removeInstance(Instance instance) {
		instances.remove(instance.getName());
	}

}
