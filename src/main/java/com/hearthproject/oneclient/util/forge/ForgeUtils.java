package com.hearthproject.oneclient.util.forge;

import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.forge.ForgeVersions;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ForgeUtils {

	private static ForgeVersions forgeVersions = null;

	public static ForgeVersions.ForgeVersion getForgeVersion(String version) {
		for (ForgeVersions.ForgeVersion forgeVersion : forgeVersions.number.values()) {
			if (forgeVersion.version.equalsIgnoreCase(version.split("-")[1])) {
				return forgeVersion;
			}
		}
		return null;
	}

	public static void downloadForgeJar(File path, String forgeVer) {
		try {
			ForgeVersions.ForgeVersion version = getForgeVersion(forgeVer);
			OneClientLogging.log("Downloading forge jar to " + path.getAbsolutePath());
			URL forgeJar = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + forgeVer + "/forge-" + forgeVer + "-universal.jar");
			if (version.branch != null && !version.branch.isEmpty()) {
				forgeJar = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + forgeVer + "-" + version.branch + "/forge-" + forgeVer + "-" + version.branch + "-universal.jar");
			}
			FileUtils.copyURLToFile(forgeJar, path);
		} catch (Throwable throwable) {
			OneClientLogging.log(throwable);
		}
	}

	//http://files.minecraftforge.net/maven/net/minecraftforge/forge/json
	public static ForgeVersions loadForgeVerions() throws IOException {
		if (forgeVersions != null) {
			return forgeVersions;
		}
		SplashScreen.updateProgess("Downloading forge version json", 30);
		String jsonStr = IOUtils.toString(new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/json"));
		SplashScreen.updateProgess("Reading forge version json", 35);
		forgeVersions = JsonUtil.GSON.fromJson(jsonStr, ForgeVersions.class);
		return forgeVersions;
	}

}
