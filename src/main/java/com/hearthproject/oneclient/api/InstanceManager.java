package com.hearthproject.oneclient.api;

import com.google.gson.JsonObject;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.File;
import java.util.Iterator;

public class InstanceManager {
	protected static ObservableMap<String, Instance> INSTANCES_MAP = FXCollections.observableHashMap();

	public static ObservableList<Instance> getInstances() {
		return FXCollections.observableArrayList(INSTANCES_MAP.values());
	}

	public static void addInstance(Instance instance) {
		if (!INSTANCES_MAP.containsKey(instance.getName())) {
			INSTANCES_MAP.put(instance.getName(), instance);
			instance.verifyMods();
			save();
		}
	}

	public static void save() {
		INSTANCES_MAP.values().forEach(Instance::save);
	}

	public static void verify() {
		OneClientLogging.logger.info("Verifying Instances");
		SplashScreen.updateProgess("Verifying instances", 10);
		for (Iterator<Instance> iterator = INSTANCES_MAP.values().iterator(); iterator.hasNext(); ) {
			Instance instance = iterator.next();
			if (!instance.getDirectory().exists()) {
				INSTANCES_MAP.remove(instance.getName());
			} else {
				instance.verifyMods();
			}
		}
	}

	public static void load() {
		OneClientLogging.logger.info("Loading Instances");
		SplashScreen.updateProgess("Loading instances", 10);
		INSTANCES_MAP.clear();
		File[] dirs = Constants.INSTANCEDIR.listFiles(File::isDirectory);
		if (dirs != null) {
			for (File dir : dirs) {
				Instance instance = load(dir);
				if (instance != null) {
					addInstance(instance);
				}
			}
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

	public static void removeInstance(Instance instance) {
		INSTANCES_MAP.remove(instance.getName());
	}

}
