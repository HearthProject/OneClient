package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;

import java.io.File;

public class HeathInstance implements Instance {

	public String name;
	public String packVersion;
	public String gameVersion;
	public String forgeVersion;
	public String url;
	public String icon;
	public Info[] info;

	public ModInstaller installer;

	public HeathInstance(String name, String packVersion, String gameVersion, String url, ModInstaller installer, Info... info) {
		this.name = name;
		this.packVersion = packVersion;
		this.gameVersion = gameVersion;
		this.url = url;
		this.installer = installer;
		this.info = info;
	}

	public String getName() {
		return name;
	}

	public String getPackVersion() {
		return packVersion;
	}

	public String getGameVersion() {
		return gameVersion;
	}

	public String getForgeVersion() {
		return forgeVersion;
	}

	public void setForgeVersion(String forgeVersion) {
		this.forgeVersion = forgeVersion;
	}

	public String getUrl() {
		return url;
	}

	public File getDirectory() {
		return FileUtil.findDirectory(Constants.INSTANCEDIR, getName());
	}

	public void install() {
		JsonUtil.save(new File(getDirectory(), "instance.json"), toString());
		installer.install(this);
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
