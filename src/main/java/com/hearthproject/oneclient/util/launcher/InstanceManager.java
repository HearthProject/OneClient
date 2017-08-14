package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
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

	public static void load() {
		instances.clear();
		if (!Constants.INSTANCEDIR.exists()) {
			Constants.INSTANCEDIR.mkdirs();
		}
		Arrays.stream(Constants.INSTANCEDIR.listFiles()).filter(File::isDirectory).forEach(dir -> {
			try {
				File jsonFile = new File(dir, "instance.json");
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
