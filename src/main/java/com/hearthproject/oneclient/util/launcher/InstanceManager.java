package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

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
		File dir = new File(Constants.INSTANCEDIR, instance.name);
		if (dir.exists()) {
			return false;
		}
		return true;
	}

	public static void save() {
		instances.values().forEach(InstanceManager::save);
	}

	public static void save(Instance instance) {
		File dir = new File(Constants.INSTANCEDIR, instance.name);
		File jsonFile = new File(dir, "instance.json");
		String jsonStr = JsonUtil.GSON.toJson(instance);
		try {
			FileUtils.writeStringToFile(jsonFile, jsonStr, StandardCharsets.UTF_8);
		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}

	public static void init(Instance instance) {
		File instanceDir = new File(Constants.INSTANCEDIR, instance.name);
		for(String dir: Constants.INITIALIZE_DIRS) {
			File d = new File(instanceDir,dir);
			if(!d.exists())
				d.mkdir();
		}
	}

	public static void load() {
		SplashScreen.updateProgess("Loading instances", 10);
		instances.clear();
		if (!Constants.INSTANCEDIR.exists()) {
			Constants.INSTANCEDIR.mkdirs();
		}
		Arrays.stream(Constants.INSTANCEDIR.listFiles()).filter(File::isDirectory).forEach(dir -> {
			try {
				File jsonFile = new File(dir, "instance.json");
				if (!jsonFile.exists()) {
					OneClientLogging.log("ERROR: An invalid instance with the name " + dir.getName() + " is has been found, it will be ignored.");
					return;
				}
				String jsonStr = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
				Instance instance = JsonUtil.GSON.fromJson(jsonStr, Instance.class);
				instances.put(instance.name, instance);
			} catch (IOException e) {
				OneClientLogging.log(e);
			}
		});
	}

	public static void removeInstance(Instance instance) {
		save();
	}

}
