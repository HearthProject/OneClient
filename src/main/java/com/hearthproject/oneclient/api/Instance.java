package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Instance {

	public String name;
	public String packVersion;
	public String gameVersion;
	public String forgeVersion;
	public String url;
	public String icon;
	public Info[] info;

	public ModInstaller installer;

	public Instance(String name, String packVersion, String gameVersion, String url, ModInstaller installer, Info... info) {
		this.name = name;
		this.packVersion = packVersion;
		this.gameVersion = gameVersion;
		this.url = url;
		this.installer = installer;
		this.info = info;
		this.icon = "icon.png";
	}

	public Instance() {
		this.icon = "icon.png";
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

	public File getModDirectory() {
		return FileUtil.findDirectory(getDirectory(), "mods");
	}

	public File getIcon() {
		File file = new File(getDirectory(), icon);
		if (!file.exists()) {
			try {
				FileUtils.copyInputStreamToFile(FileUtil.getResource("images/modpack.png"), file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void install() {
		FileUtil.createDirectory(getDirectory());
		if (installer != null)
			installer.install(this);
	}

	public void delete() {
		getDirectory().delete();
	}

	public void update() {

	}

	public void save() {
		JsonUtil.save(new File(getDirectory(), "instance.json"), toString());
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}