package com.hearthproject.oneclient.util.minecraft;

import com.google.gson.Gson;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.forge.ForgeVersions;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.AssetIndex;
import com.hearthproject.oneclient.json.models.minecraft.AssetObject;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.minecraft.Version;
import com.hearthproject.oneclient.json.models.minecraft.launcher.LauncherProfile;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class MinecraftUtil {

	// http://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.jar
	// https://launcher.mojang.com/download/MinecraftInstaller.msi

	private static GameVersion version = null;

	public static GameVersion loadGameVersions() throws Exception {
		if (version == null) {
			SplashScreen.updateProgess("Downloading minecraft version json", 20);
			String data = IOUtils.toString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), StandardCharsets.UTF_8);
			SplashScreen.updateProgess("Reading version json", 25);
			version = JsonUtil.GSON.fromJson(data, GameVersion.class);
			return version;
		}
		return version;
	}

	public static Version downloadMcVersionData(String minecraftVersion) {
		try {
			Optional<GameVersion.Version> optionalVersion = version.versions.stream().filter(versions -> versions.id.equalsIgnoreCase(minecraftVersion)).findFirst();
			if (optionalVersion.isPresent()) {
				String jsonData = IOUtils.toString(new URL(optionalVersion.get().url), StandardCharsets.UTF_8);
				return JsonUtil.GSON.fromJson(jsonData, Version.class);
			} else {
				OneClientLogging.log(new RuntimeException("Failed downloading Minecraft json"));
				return null;
			}
		} catch (Throwable throwable) {
			OneClientLogging.log(throwable);
			return null;
		}
	}

	public static void installMinecraft(Instance instance) throws Throwable {
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File assets = new File(mcDir, "assets");
		File versions = new File(mcDir, "versions");
		File libraries = new File(mcDir, "libraries");
		File natives = new File(mcDir, "natives");

		Version versionData = downloadMcVersionData(instance.minecraftVersion);
		File mcJar = new File(versions, instance.minecraftVersion + ".jar");

		OneClientLogging.log("Downloading minecraft jar");
		if(!MiscUtil.checksumEquals(mcJar, versionData.downloads.get("client").sha1)){
			FileUtils.copyURLToFile(new URL(versionData.downloads.get("client").url), mcJar);
		}

		OneClientLogging.log("Starting download of " + versionData.libraries.size() + " library's");
		for (Version.Library library : versionData.libraries) {
			if (library.allowed() && library.getFile(libraries) != null) {
				if(library.getFile(libraries).exists()){
					if(MiscUtil.checksumEquals(library.getFile(libraries), library.getSha1())){
						continue;
					}
				}
				OneClientLogging.log("Downloading " + library.name + " from " + library.getURL());
				FileUtils.copyURLToFile(new URL(library.getURL()), library.getFile(libraries));
			}
		}

		versionData.libraries.stream().filter(lib -> lib.natives != null && lib.allowed()).forEach(library -> {
					OneClientLogging.log("Extracting native " + library.name);
					ZipUtil.unpack(library.getFile(libraries), natives);
		});

		Version.AssetIndex assetIndex = versionData.assetIndex;
		File assetsInfo = new File(new File(assets, "objects"), "indexes" + File.separator + assetIndex.id + ".json");
		FileUtils.copyURLToFile(new URL(assetIndex.url), assetsInfo);
		AssetIndex index = new Gson().fromJson(new FileReader(assetsInfo), AssetIndex.class);
		Map<String, AssetObject> parent = index.getFileMap();

		OneClientLogging.log("Starting download of " + parent.entrySet().size() + " assets");
		for (Map.Entry<String, AssetObject> entry : parent.entrySet()) {
			AssetObject object = entry.getValue();
			String sha1 = object.getHash();
			File file = new File(assets, "objects" + File.separator + sha1.substring(0, 2) + File.separator + sha1);
			if (!file.exists() || !MiscUtil.checksumEquals(file, sha1)) {
				OneClientLogging.log("Downloading asset " + entry.getKey() + " from " + Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1 + " to " + file);
				FileUtils.copyURLToFile(new URL(Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1), file);
			}
		}
	}

	public static void startMinecraft(Instance instance){
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File assets = new File(mcDir, "assets");
		File versions = new File(mcDir, "versions");
		File libraries = new File(mcDir, "libraries");
		File natives = new File(mcDir, "natives");
		Version versionData = downloadMcVersionData(instance.minecraftVersion);
		File mcJar = new File(versions, instance.minecraftVersion + ".jar");
		System.out.println(mcJar.exists());

		OneClientLogging.log("Starting minecraft...");

		new Thread(() -> {
			try {

				StringBuilder cpb = new StringBuilder();
				for (Version.Library library : versionData.libraries) {
					if (library.allowed() && library.getFile(libraries) != null) {
						cpb.append(OperatingSystem.getJavaDelimiter());
						cpb.append(library.getFile(libraries).getAbsolutePath());
					}
				}
				cpb.append(OperatingSystem.getJavaDelimiter());
				cpb.append(mcJar.getAbsolutePath());

				ArrayList<String> arguments = new ArrayList<>();
				arguments.add("java");

				arguments.add("-Djava.library.path=" + natives.getAbsolutePath());

				arguments.add("-cp");
				arguments.add(cpb.toString());
				arguments.add(versionData.mainClass);

				arguments.add("--accessToken=123");
				arguments.add("--version=" + instance.minecraftVersion);
				arguments.add("--assetsDir=" + new File(assets, "objects"));
				arguments.add("--assetIndex=" + versionData.assetIndex.id);
				arguments.add("--gameDir=" + new File(Constants.INSTANCEDIR, instance.name));

				ProcessBuilder processBuilder = new ProcessBuilder(arguments);
				processBuilder.directory(new File(Constants.INSTANCEDIR, instance.name));
				Process process = processBuilder.start();

				BufferedReader reader =
					new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					OneClientLogging.log(line);
				}

				BufferedReader readerErr =
					new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String lineErr;
				while ((lineErr = readerErr.readLine()) != null) {
					OneClientLogging.log(lineErr);
				}


			} catch (Throwable throwable) {
				OneClientLogging.log(throwable);
			}

		}).start();
	}
}
