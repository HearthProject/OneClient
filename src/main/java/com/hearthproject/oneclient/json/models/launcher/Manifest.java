package com.hearthproject.oneclient.json.models.launcher;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.modloader.IModloader;
import com.hearthproject.oneclient.json.models.modloader.forge.ForgeVersions;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;

import java.io.File;
import java.util.List;
import java.util.Optional;

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
		return minecraft.modLoaders.stream().map(l -> l.id).filter(id -> id.startsWith("forge")).map(id -> id.replace("forge-", "")).findFirst().orElse("");
	}

	public IModloader getModloader() {
		String forge = getForge();
		if (forge.isEmpty())
			return new IModloader.None();
		return new ForgeVersions.ForgeVersion(getForge());
	}

	public Optional<File> getIcon() {
		if (icon == null || icon.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new File(getDirectory(), icon));
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public static class MinecraftData {
		public String version;
		public List<Modloader> modLoaders;

		public static class Modloader {
			public String id, name, version;
			public boolean primary;

			public Modloader(String id) {
				this.primary = true;
				this.id = id;
				if (id.startsWith("forge")) {
					this.name = id.substring(0, id.indexOf('-'));
					this.version = id.substring(id.indexOf('-') + 1);
				} else {
					this.name = id;
				}
			}
		}
	}

	public static class FileData {
		public int projectID;
		public int fileID;
		public boolean required;

		@Override
		public String toString() {
			return projectID + "/" + fileID;
		}
	}

}
