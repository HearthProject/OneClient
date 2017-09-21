package com.hearthproject.oneclient.util.forge;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.modloader.IModloader;
import com.hearthproject.oneclient.json.models.modloader.forge.ForgeVersionProfile;
import com.hearthproject.oneclient.json.models.modloader.forge.ForgeVersions;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class ForgeUtils {

	private static ForgeVersions forgeVersions = null;

	public static IModloader getModloader(String version) {
		ForgeVersions.ForgeVersion forge = getForgeVersion(version);
		if (forge == null)
			return IModloader.NONE;
		return forge;
	}

	public static ForgeVersions.ForgeVersion getForgeVersion(String version) {
		for (ForgeVersions.ForgeVersion forgeVersion : forgeVersions.number.values()) {
			if (forgeVersion.version.equalsIgnoreCase(version)) {
				return forgeVersion;
			}
		}
		return null;
	}

	public static int count = 1;

	public static List<File> resolveForgeLibrarys(String mcVer, String forgeVer) throws IOException {
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File libraries = new File(mcDir, "libraries");
		ForgeVersionProfile forgeVersionProfile = downloadForgeVersion(libraries, mcVer, forgeVer);
		ArrayList<File> librarys = new ArrayList<>();
		NotifyUtil.setText("Resolving %s ForgeLibraries", forgeVersionProfile.libraries.size());
		count = 1;
		forgeVersionProfile.libraries.parallelStream().forEach(library -> {
			NotifyUtil.setProgressAscend(count++, forgeVersionProfile.libraries.size());
			if ((library.checksums == null && library.getFile(libraries).exists()) || (library.checksums != null && !library.checksums.isEmpty() && MiscUtil.checksumEquals(library.getFile(libraries), library.checksums))) {
				librarys.add(library.getFile(libraries));
				return;
			}
			try {
				OneClientLogging.logger.info("Downloading " + library.name + " from " + library.getURL());
				int response = MiscUtil.getResponseCode(new URL(library.getURL()));
				if (response == 404) {
					library.url = Constants.MAVEN_CENTRAL_BASE;
				}
				FileUtils.copyURLToFile(new URL(library.getURL()), library.getFile(libraries));
				if (!library.getFile(libraries).exists()) {
					OneClientLogging.logger.error("Error with " + library.name);
				}
				librarys.add(library.getFile(libraries));
			} catch (Exception e) {
				OneClientLogging.error(e);
			}

		});
		NotifyUtil.clear();
		return librarys;
	}

	public static ForgeVersionProfile downloadForgeVersion(File versionsDir, String mcVer, String forgeVer) throws IOException {
		JarFile jarFile = downloadForgeJar(versionsDir, mcVer, forgeVer);
		String json = IOUtils.toString(jarFile.getInputStream(jarFile.getEntry("version.json")), StandardCharsets.UTF_8);
		jarFile.close();
		return JsonUtil.GSON.fromJson(json, ForgeVersionProfile.class);
	}

	public static JarFile downloadForgeJar(File versionsDir, String mcVer, String forgeVer) throws IOException {
		ForgeVersions.ForgeVersion version = getForgeVersion(forgeVer);
		String jarName = getForgeJar(mcVer, forgeVer, null);
		File forgeJar = new File(Constants.TEMPDIR, jarName);
		File forgeSHA1 = new File(Constants.TEMPDIR, jarName + ".sha1");
		if(forgeJar.exists() && forgeSHA1.exists()){
			if(MiscUtil.checksumEquals(forgeJar, FileUtils.readFileToString(forgeSHA1, StandardCharsets.UTF_8))){
				return new JarFile(forgeJar);
			}
		}
		OneClientLogging.logger.info("Downloading forge jar to " + versionsDir.getAbsolutePath());
		if (version.branch != null && !version.branch.isEmpty()) {
			jarName = getForgeJar(mcVer, forgeVer, version.branch);
		}
		URL forgeSHA1URL = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + jarName + ".sha1");
		URL forgeJarURL = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + jarName);
		FileUtils.copyURLToFile(forgeSHA1URL, forgeSHA1);
		FileUtils.copyURLToFile(forgeJarURL, forgeJar);
		return new JarFile(forgeJar);
	}

	public static String getForgeJar(String mcVer, String forgeVer, String branch) {
		if (branch == null)
			return String.format("%s-%s/forge-%s-%s-universal.jar", mcVer, forgeVer, mcVer, forgeVer);
		return String.format("%s-%s-%s/forge-%s-%s-%s-universal.jar", mcVer, forgeVer, branch, mcVer, forgeVer, branch);
	}

	//http://files.minecraftforge.net/maven/net/minecraftforge/forge/json
	public static ForgeVersions loadForgeVersions() throws IOException {
		if (forgeVersions != null) {
			return forgeVersions;
		}
		SplashScreen.updateProgess("Downloading forge version json", 30);
		//TODO maybe cache this too
		try {
			String jsonStr = IOUtils.toString(new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/json"), Charset.defaultCharset());
			SplashScreen.updateProgess("Reading forge version json", 35);
			forgeVersions = JsonUtil.GSON.fromJson(jsonStr, ForgeVersions.class);
		} catch (UnknownHostException e) {
			OneClientLogging.error(e);
		}
		return forgeVersions;
	}

}
