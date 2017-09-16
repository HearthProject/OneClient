package com.hearthproject.oneclient.api.curse.data;

import com.google.common.collect.Lists;

import java.util.List;

public class Manifest {

	public String manifestType;
	public String manifestVersion;
	public String name;
	public String version;
	public String author;
	public int projectID;
	public List<FileData> files;
	public String overrides;
	public String icon;
	public Minecraft minecraft;

	public Manifest() {
		files = Lists.newArrayList();
	}

	public class Minecraft {
		public String version;
		public List<Modloader> modLoaders;

		public class Modloader {
			public String id;
			public boolean primary;
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
