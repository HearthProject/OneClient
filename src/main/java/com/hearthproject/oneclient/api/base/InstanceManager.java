package com.hearthproject.oneclient.api.base;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.modpack.curse.CurseImporter;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.File;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class InstanceManager {
    protected static ObservableMap<String, Instance> INSTANCES_MAP = FXCollections.observableHashMap();
    protected static ObservableList<Instance> INSTANCES = FXCollections.observableArrayList();
    public static ObservableList<Instance> FEATURED = FXCollections.observableArrayList();
    public static ObservableList<Instance> RECENT = FXCollections.observableArrayList();

    static {
        INSTANCES_MAP.addListener((MapChangeListener<? super String, ? super Instance>) change -> {
            INSTANCES.addAll(change.getValueAdded());
            INSTANCES.removeAll(change.getValueRemoved());
        });
    }

    public static ObservableList<Instance> getInstances() {
        return INSTANCES;
    }

    public static void addInstance(Instance instance) {
        MiscUtil.runLaterIfNeeded(() -> {
            if (!INSTANCES_MAP.containsKey(instance.getName())) {
                INSTANCES_MAP.put(instance.getName(), instance);
                addRecent(instance);
                instance.verifyMods();
            }
        });
    }

    public static void save() {
        INSTANCES_MAP.values().forEach(Instance::save);
    }

    public static void verify() {
        OneClientLogging.logger.info("Verifying Instances");
        SplashScreen.updateProgess("Verifying instances", 10);

        for (Iterator<Instance> iterator = INSTANCES_MAP.values().iterator(); iterator.hasNext(); ) {
            Instance instance = iterator.next();
            if (!instance.getDirectory().exists() || !(instance.getDirectory().exists())) {
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
                if (instance != null && !INSTANCES_MAP.containsKey(instance.getName())) {
                    addInstance(instance);
                }
            }
        }
    }


    private static Instance load(File dir) {
        File instanceJson = new File(dir, "instance.json");
        return JsonUtil.read(instanceJson, Instance.class);
    }

    public static void removeInstance(Instance instance) {
        INSTANCES_MAP.remove(instance.getName());
        verify();
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
        private String name;
        private int projectId;
    }

    public static List<Featured> getFeaturedProjects() {

        Type list = new TypeToken<List<Featured>>() {
        }.getType();

        try {
            return JsonUtil.read(new URL(FEATURED_URL), list);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObservableList<Instance> getFeaturedInstances() {
        ObservableList<Instance> list = FXCollections.observableArrayList();
        List<Featured> featured = getFeaturedProjects();
        if (featured != null) {
            for (Featured project : featured) {
                Instance instance = new CurseImporter(project.name, project.projectId).create();
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
