package com.hearthproject.oneclient.json.models.launcher;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.modloader.IModloader;
import com.hearthproject.oneclient.json.models.modloader.forge.ForgeVersions;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;

import java.io.File;
import java.util.List;

public class Manifest {

	public MinecraftData minecraft;
	public String manifestType;
	public String manifestVersion;
	public String name;
	public String version;
	public String author;
	public int projectID;
	public List<FileData> files;
	public String overrides;
	public String icon;

	public Manifest() {
		files = Lists.newArrayList();
	}

	public MinecraftData getMinecraft() {
		if (minecraft == null)
			minecraft = new MinecraftData();
		return minecraft;
	}

	public File getDirectory() {
		return FileUtil.getDirectory(Constants.INSTANCEDIR, name);
	}

	public String getMinecraftVersion() {
		return getMinecraft().version;
	}

	public void setMinecraftVersion(String version) {
		getMinecraft().version = version;
	}

	public GameVersion.Version getGameVersion() {
		try {
			return MinecraftUtil.loadGameVersions().get(v -> v.id.equalsIgnoreCase(getMinecraftVersion())).findFirst().orElse(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setModloader(String modloader) {
		getMinecraft().modLoaders = Lists.newArrayList(new MinecraftData.Modloader(modloader));
	}

	public String getForge() {
		for (MinecraftData.Modloader m : minecraft.modLoaders) {
			if (m.getName().equals("forge")) {
				return m.getVersion();
			}
		}
		return "";
	}

	public IModloader getModloader() {
		String forge = getForge();
		if (forge == null || forge.isEmpty())
			return new IModloader.None();
		return new ForgeVersions.ForgeVersion(forge);
	}

	public File getIcon() {
		if (icon == null || icon.isEmpty()) {
			return FileUtil.getResource("images/modpack.png");
		}
		return new File(getDirectory(), icon);
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static class MinecraftData {
		public String version;
		public List<Modloader> modLoaders = Lists.newArrayList();

		public static class Modloader {
			public String id;
			public boolean primary;

			public String getName() {
				int i = id.indexOf('-');
				if (i > 0)
					return id.substring(0, id.indexOf('-'));
				return id;
			}

			public String getVersion() {
				int i = id.indexOf('-');
				if (i > 0)
					return id.substring(i + 1);
				return "";
			}

			public Modloader(String id) {
				this.primary = true;
				this.id = id;
			}
		}
	}

	public void save() {
		String manifest = JsonUtil.GSON.toJson(this);
		JsonUtil.save(new File(getDirectory(), "manifest.json"), manifest);
	}
}
