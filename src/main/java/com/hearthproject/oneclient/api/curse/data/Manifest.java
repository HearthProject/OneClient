package com.hearthproject.oneclient.api.curse.data;

import com.google.common.collect.Lists;

import java.util.List;

public class Manifest {

	public int manifestVersion;
	public String manifestType;
	public String name;
	public String version;
	public String author;
	public String projectID;
	public List<FileData> files;
	public String overrides;
	public String icon;
	public Minecraft minecraft;

	public Manifest() {
		files = Lists.newArrayList();
	}

	public static class Minecraft {
		public String version;
		public List<Modloader> modLoaders = Lists.newArrayList();

		public static class Modloader {
			public String id;
			public boolean primary;

			public Modloader(String forgeVersion) {
				this.id = "forge-" + forgeVersion;
				this.primary = true;
			}
		}

		public String getModloader() {
			for (Modloader modloader : modLoaders) {
				if (modloader.id != null) {
					return modloader.id.replace("forge-", "");
				}
			}
			return "";
		}
	}
}
