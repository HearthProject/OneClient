package com.hearthproject.oneclient.util.minecraft;

import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MinecraftUtil {

	private static GameVersion version = null;

	public static GameVersion loadGameVersion() throws IOException {
		if(version == null){
			String data = IOUtils.toString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), StandardCharsets.UTF_8);
			version = JsonUtil.GSON.fromJson(data, GameVersion.class);
			return version;
		}
		return version;
	}

}
