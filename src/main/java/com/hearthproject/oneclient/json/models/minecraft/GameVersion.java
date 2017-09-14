package com.hearthproject.oneclient.json.models.minecraft;

import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

// https://launchermeta.mojang.com/mc/game/version_manifest.json
public class GameVersion {

	public Latest latest;
	public List<VersionData> versions;

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
