package com.hearthproject.oneclient.util.forge;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.forge.ForgeVersions;
import com.hearthproject.oneclient.util.ClasspathUtils;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

public class ForgeUtils {

	private static ForgeVersions forgeVersions = null;

	public static InputStream VERSION_PROFILE;

	private static String forgeVer;

	//1.12-14.21.1.2443
	public static void installForge(File file, String forgeVersion) throws IOException {
		forgeVer = forgeVersion;
		//http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12-14.21.1.2443/forge-1.12-14.21.1.2443-installer.jar
		URL forgeInstallerURL = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + forgeVersion + "/forge-" + forgeVersion + "-installer.jar");
		File forgeInstaller = new File(Constants.TEMPDIR, "forge-installer-" + forgeVersion + ".jar");
		if (!forgeInstaller.exists()) {
			FileUtils.copyURLToFile(forgeInstallerURL, forgeInstaller);
		}
		JarFile jarFile = new JarFile(forgeInstaller);
		OneClientLogging.log("Reading install_profile.json from " + file.getName());

		VERSION_PROFILE = jarFile.getInputStream(jarFile.getEntry("install_profile.json"));
		String jsonSTR = IOUtils.toString(VERSION_PROFILE, StandardCharsets.UTF_8);
		System.out.println(jsonSTR);
		VERSION_PROFILE = IOUtils.toInputStream(jsonSTR, StandardCharsets.UTF_8);
		ForgeInstaller.installForge(file);
	}

	public static void downloadForgeJar(File path)  {
		try {
			OneClientLogging.log("Downloading forge jar to " + path.getAbsolutePath());
			URL forgeJar = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + forgeVer + "/forge-" + forgeVer + "-universal.jar");
			FileUtils.copyURLToFile(forgeJar, path);
		} catch (Throwable throwable){
			OneClientLogging.log(throwable);
		}
	}

	//http://files.minecraftforge.net/maven/net/minecraftforge/forge/json
	public static ForgeVersions loadForgeVerions() throws IOException {
		if (forgeVersions != null) {
			return forgeVersions;
		}
		String jsonStr = IOUtils.toString(new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/json"));
		forgeVersions = JsonUtil.GSON.fromJson(jsonStr, ForgeVersions.class);
		return forgeVersions;
	}

}
