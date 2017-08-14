package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.json.models.launcher.Instance;

import java.io.File;
import java.util.ArrayList;

public class InstanceManager {

    protected static ArrayList<Instance> instances = new ArrayList<>();

    public static ArrayList<Instance> getInstances() {
        return instances;
    }

    public static void addInstance(Instance instance) {

    }

    public static void saveInstance(Instance instance, File dir) {

    }

    public static void importInstance(File instanceDir) {

    }

    public static void removeInstance(Instance instance) {

    }

    public static void updateInstance(String name, Instance instance) {

    }

}
