package com.hearthproject.oneclient.util.json.models.minecraft;

import com.google.common.collect.Maps;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

// https://launchermeta.mojang.com/mc/game/version_manifest.json
public class GameVersion {

	private final HashMap<String, VersionData> DATA = Maps.newHashMap();

	public Latest latest;
	public List<VersionData> versions;

	public VersionData getVersionData(String version) {
		if (DATA.isEmpty()) {
			for (VersionData data : versions) {
				DATA.put(data.id, data);
			}
		}
		return DATA.get(version);
	}

	public class Latest {
		public String snapshot;
		public String release;
	}

	public class VersionData {
		public String id;
		public String type;
		public String time;
		public String releaseTime;
		public String url;

		@Override
		public String toString() {
			return id;
		}

		public Version getData() {
			File v = new File(MinecraftUtil.VERSIONS, id + ".json");
			if (!v.exists()) {
				OneClientLogging.info("Downloading Minecraft Data {} ", url);
				FileUtil.downloadFromURL(url, v);
			}
			return JsonUtil.read(v, Version.class);
		}
	}

	public Stream<VersionData> get(Predicate<VersionData> filter) {
		return versions.stream().filter(filter);
	}

}
