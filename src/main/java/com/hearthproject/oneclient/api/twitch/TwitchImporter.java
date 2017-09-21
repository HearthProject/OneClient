package com.hearthproject.oneclient.api.twitch;

import com.google.gson.JsonObject;
import com.hearthproject.oneclient.api.IImporter;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.json.JsonUtil;

import java.io.File;

public class TwitchImporter implements IImporter {

	private File twitchFolder;

	public TwitchImporter(File twitchFolder) {
		this.twitchFolder = twitchFolder;
	}

	@Override
	public Instance create() {
		File minecraftinstance = new File(twitchFolder, "minecraftinstance.json");
		JsonObject json = JsonUtil.read(minecraftinstance, JsonObject.class);
		String name = json.get("name").getAsString();
		String gameVersion = json.getAsJsonObject("baseModLoader").get("MinecraftVersion").getAsString();
		String forgeVersion = json.getAsJsonObject("baseModLoader").get("Name").getAsString().replace("forge-", "");

		TwitchInstaller installer = new TwitchInstaller(twitchFolder);

		Instance instance = new Instance(name, "", installer);
		instance.setGameVersion(gameVersion);
		instance.setForgeVersion(forgeVersion);

		return instance;
	}
}
