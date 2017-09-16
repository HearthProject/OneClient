package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Instance {

	public String name;
	public String packVersion;
	public String gameVersion;
	public String forgeVersion;
	public String url;
	public String icon;
	public Map<String, Object> info;

	public transient Image image;

	public transient ModInstaller installer;

	public Instance(String name, String url, ModInstaller installer, Pair<String, Object>... info) {
		this();
		this.name = name;
		this.url = url;
		this.installer = installer;
		this.info = Arrays.stream(info).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}

	public Instance() {
		this.icon = "icon.png";
		this.forgeVersion = "";
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

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void install() {
		FileUtil.createDirectory(getDirectory());
		if (installer != null)
			installer.install(this);
		save();
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

	public ModInstaller getInstaller() {
		return installer;
	}
}