package com.hearthproject.oneclient.json.models.launcher;

public class Instance {

    public String name;
    //This is here to allow support for more than just mc in the future
    public String game = "Minecraft";

    public MinecraftData minecraftData;

    public String gameDir;

    public String icon;

    public long lastLaunch;

    public long playTime;

    public Instance(String name) {
        this.name = name;
        icon = "";
    }

    public class MinecraftData {
        String gameVersion;

        String modLoader;
    }

}
