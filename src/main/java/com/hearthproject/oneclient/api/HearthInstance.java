package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.json.JsonUtil;

import java.io.File;

public class HearthInstance implements Instance {

	public String name;
	public String packVersion;
	public String gameVersion;
	public String forgeVersion;
	public String url;
	public String icon;
	public Info[] info;

	public ModInstaller installer;

	public HearthInstance(String name, String packVersion, String gameVersion, String url, ModInstaller installer, Info... info) {
		this.name = name;
		this.packVersion = packVersion;
		this.gameVersion = gameVersion;
		this.url = url;
		this.installer = installer;
		this.info = info;
		this.icon = "icon.png";
	}

	public HearthInstance() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPackVersion(String packVersion) {
		this.packVersion = packVersion;
	}

	public void setGameVersion(String gameVersion) {
		this.gameVersion = gameVersion;
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
		return new File(Constants.INSTANCEDIR, getName());
	}

	public File getIcon() {
		return new File(getDirectory(), icon);
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void install() {
		FileUtil.createDirectory(getDirectory());
		if (installer != null)
			installer.install(this);
	}

	@Override
	public void delete() {
		getDirectory().delete();
	}

	@Override
	public void update() {
		//TODO
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
