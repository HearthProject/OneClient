package com.hearthproject.oneclient.util.forge;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.forge.ForgeVersionProfile;
import com.hearthproject.oneclient.json.models.forge.ForgeVersions;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

	public static List<File> resloveForgeLibrarys(String forgeVer) throws IOException {
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File libraries = new File(mcDir, "libraries");
		ForgeVersionProfile forgeVersionProfile = downloadForgeVersion(libraries, forgeVer);
		ArrayList<File> librarys = new ArrayList<>();
		OneClientLogging.log("Resolving " + forgeVersionProfile.libraries.size() + " forge library's");
		for(ForgeVersionProfile.Library library : forgeVersionProfile.libraries){
			if(library.checksums != null && !library.checksums.isEmpty() && MiscUtil.checksumEquals(library.getFile(libraries), library.checksums)){
				librarys.add(library.getFile(libraries));
				continue;
			}
			OneClientLogging.log("Downloading " + library.name + " from " + library.getURL());
			int response = MiscUtil.getResponseCode(new URL(library.getURL()));
			if(response == 404){
				library.url = Constants.MAVEN_CENTRAL_BASE;
			}
			FileUtils.copyURLToFile(new URL(library.getURL()), library.getFile(libraries));
			librarys.add(library.getFile(libraries));
		}
		return librarys;
	}

	public static ForgeVersionProfile downloadForgeVersion(File versionsDir, String forgeVer) throws IOException {
		JarFile jarFile = downloadForgeJar(versionsDir, forgeVer);
		String json = IOUtils.toString(jarFile.getInputStream(jarFile.getEntry("version.json")), StandardCharsets.UTF_8);
		jarFile.close();
		return JsonUtil.GSON.fromJson(json, ForgeVersionProfile.class);
	}

	public static JarFile downloadForgeJar(File versionsDir, String forgeVer) throws IOException {
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
	}

	//http://files.minecraftforge.net/maven/net/minecraftforge/forge/json
	public static ForgeVersions loadForgeVersions() throws IOException {
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
