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
import java.util.jar.JarFile;

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

	public static JarFile downloadForgeJar(File versionsDir, String forgeVer) {
		try {
			ForgeVersions.ForgeVersion version = getForgeVersion(forgeVer);
			String jarName = forgeVer + "/forge-" + forgeVer + "-universal.jar";
			File forgeJar = new File(forgeVer, jarName);
			OneClientLogging.log("Downloading forge jar to " + versionsDir.getAbsolutePath());
			if (version.branch != null && !version.branch.isEmpty()) {
				jarName =  forgeVer + "-" + version.branch + "/forge-" + forgeVer + "-" + version.branch + "-universal.jar";
			}
			URL forgeJarURL = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + jarName);
			FileUtils.copyURLToFile(forgeJarURL, forgeJar);
			return new JarFile(forgeJar);
		} catch (Throwable throwable) {
			OneClientLogging.log(throwable);
		}
		return null;
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
