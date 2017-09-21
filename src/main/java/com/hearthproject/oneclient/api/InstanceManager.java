package com.hearthproject.oneclient.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.curse.CurseImporter;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
			MiscUtil.runLaterIfNeeded( () -> {
				if(ContentPanes.INSTANCES_PANE.gridView != null)
					ContentPanes.INSTANCES_PANE.gridView.setItems(getInstances());
			});
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

	private static List<String> RECENT_INSTANCES = Lists.newArrayList();
	private static final int MAX_RECENT = 4;

	private static void loadRecent() {
		String[] instances = JsonUtil.read(new File(Constants.INSTANCEDIR, "recent.json"), String[].class);
		if (instances != null) {
			RECENT_INSTANCES = Lists.newArrayList(instances);
			RECENT_INSTANCES.removeIf(instance -> !INSTANCES_MAP.containsKey(instance));
		}
	}

	private static void saveRecent() {
		JsonUtil.save(new File(Constants.INSTANCEDIR, "recent.json"), JsonUtil.GSON.toJson(RECENT_INSTANCES));
	}

	public static void addRecent(Instance instance) {
		loadRecent();
		if (RECENT_INSTANCES.size() > MAX_RECENT) {
			RECENT_INSTANCES.remove(RECENT_INSTANCES.size() - 1);
		}
		if (!RECENT_INSTANCES.contains(instance.getName()))
			RECENT_INSTANCES.add(0, instance.getName());
		saveRecent();
	}

	public static String FEATURED_URL = "http://fdn.redstone.tech/theoneclient/oneclient/featured.json";

	private static class Featured {
		private String name, projectId;
	}

	public static List<String> getFeaturedProjects() {
		Featured[] array = null;
		try {
			array = JsonUtil.read(new URL(FEATURED_URL), Featured[].class);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (array != null) {
			return Arrays.stream(array).map(f -> f.projectId).collect(Collectors.toList());
		}
		return null;
	}

	public static ObservableList<Instance> getFeaturedInstances() {
		ObservableList<Instance> list = FXCollections.observableArrayList();
		List<String> featured = getFeaturedProjects();
		if (featured != null) {
			for (String project : featured) {
				Instance instance = new CurseImporter(project).create();
				if (INSTANCES_MAP.containsKey(instance.getName())) {
					instance.setInstalling(true);
				}
				list.add(instance);
			}
		}
		return list;
	}

	public static ObservableList<Instance> getRecentInstances() {
		loadRecent();
		if (getInstances().isEmpty())
			return FXCollections.emptyObservableList();
		List<Instance> recent = Lists.newArrayList();
		for (String name : RECENT_INSTANCES) {
			if (INSTANCES_MAP.containsKey(name))
				recent.add(INSTANCES_MAP.get(name));
		}
		if (recent.isEmpty()) {
			int size = getInstances().size();
			return FXCollections.observableArrayList(getInstances().subList(0, Math.min(size, MAX_RECENT)));
		}
		return FXCollections.observableArrayList(recent);
	}
}
