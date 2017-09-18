package com.hearthproject.oneclient.util.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.LogController;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.minecraft.AssetIndex;
import com.hearthproject.oneclient.json.models.minecraft.AssetObject;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.minecraft.Version;
import com.hearthproject.oneclient.json.models.modloader.forge.ForgeVersionProfile;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.tracking.OneClientTracking;
import com.mojang.authlib.properties.PropertyMap;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MinecraftUtil {

	// http://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.jar
	// https://launcher.mojang.com/download/MinecraftInstaller.msi

	public static ObservableList<String> MINECRAFT_VERSIONS;
	private static GameVersion version = null;

	public static File ASSETS;
	public static File VERSIONS;
	public static File LIBRARIES;
	public static File NATIVES;
	public static File VERSION_MANIFEST;

	public static void load() {
		ASSETS = new File(Constants.MINECRAFTDIR, "assets");
		VERSIONS = new File(Constants.MINECRAFTDIR, "versions");
		LIBRARIES = new File(Constants.MINECRAFTDIR, "libraries");
		NATIVES = new File(Constants.MINECRAFTDIR, "natives");
		VERSION_MANIFEST = new File(VERSIONS, "version_manifest.json");
		parseGameVersions();
		MINECRAFT_VERSIONS = getVersions(false);
	}

	private static String parseVersionManifest() throws UnknownHostException {
		SplashScreen.updateProgess("Downloading minecraft version json", 20);
		try {
			FileUtil.downloadFromURL(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), VERSION_MANIFEST);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			if (!VERSION_MANIFEST.exists()) {
				OneClientLogging.logUserError(new FileNotFoundException("Minecraft version manifest was not found. "
					+ "\nIf playing in offline mode try starting the game with an internet connection."), "Error playing minecraft");
			}
			return IOUtils.toString(VERSION_MANIFEST.toURI(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SplashScreen.updateProgess("Reading version json", 25);
		return null;
	}

	private static void parseGameVersions() {
		if (version == null) {
			String data = null;
			try {
				data = parseVersionManifest();
			} catch (UnknownHostException e) {
				OneClientLogging.error(e);
			}
			if (data != null)
				version = JsonUtil.GSON.fromJson(data, GameVersion.class);
		}
	}

	public static GameVersion getGameVersionData() {
		if (version == null)
			parseGameVersions();
		return version;
	}

	public static GameVersion.VersionData getVersionData(String version) {
		return getGameVersionData().get(versions -> versions.id.equalsIgnoreCase(version)).findFirst().orElse(null);
	}

	public static Version getVersion(String version) {
		GameVersion.VersionData versionData = getVersionData(version);
		if (versionData != null)
			return versionData.getData();
		return null;
	}

	public static ObservableList<String> getVersions(final boolean snapshots) {
		ObservableList<String> versions = getGameVersionData().versions.stream().filter(p -> p.isRelease() || snapshots).map(GameVersion.VersionData::getId).collect(MiscUtil.toObservableList());
		versions.add(0, "All");
		return versions;
	}

	public static int i = 1, count;

	public static void installMinecraft(Instance instance) throws Throwable {
		InstanceManager.setInstanceInstalling(instance, true);
		NotifyUtil.setText("Installing minecraft for " + instance.getName());
		OneClientTracking.sendRequest("minecraft/install/" + instance.getGameVersion());

		Version versionData = getVersion(instance.getGameVersion());
		File mcJar = new File(VERSIONS, instance.getGameVersion() + ".jar");

		OneClientLogging.logger.info("Downloading Minecraft jar");
		if (!MiscUtil.checksumEquals(mcJar, versionData.downloads.get("client").sha1)) {
			FileUtils.copyURLToFile(new URL(versionData.downloads.get("client").url), mcJar);
		}

		i = 1;
		count = versionData.libraries.size();
		NotifyUtil.setText("Resolving %s Libraries", count);
		OneClientLogging.info("{}", versionData.libraries);
		versionData.libraries.forEach(library -> {
			NotifyUtil.setProgressAscend(i++, count);
			if (library.allowed() && library.getFile(LIBRARIES) != null) {

				OneClientLogging.info("Resolving Library {}", library.name, i, count);
				if (library.getFile(LIBRARIES).exists()) {
					if (MiscUtil.checksumEquals(library.getFile(LIBRARIES), library.getSha1())) {
						OneClientLogging.info("Skipping: Library {} already found", library.name, i, count);
						return;
					}
				}
				try {
					File l = library.getFile(LIBRARIES);
					FileUtils.copyURLToFile(new URL(library.getURL()), l);
					OneClientLogging.logger.info("Downloading " + library.name + " from " + library.getURL() + " to " + l);
				} catch (IOException e) {
					OneClientLogging.error(e);
				}
			}
		});

		versionData.libraries.stream().filter(lib -> lib.natives != null && lib.allowed()).forEach(library -> {
			OneClientLogging.logger.info("Extracting native " + library.name);
			File file = library.getFile(LIBRARIES);
			if (file.exists())
				ZipUtil.unpack(file, NATIVES);
		});

		if (!instance.getForgeVersion().isEmpty())
			ForgeUtils.resolveForgeLibrarys(instance.getGameVersion(), instance.getForgeVersion());

		Version.AssetIndex assetIndex = versionData.assetIndex;
		File assetsInfo = new File(ASSETS, "indexes" + File.separator + assetIndex.id + ".json");
		FileUtils.copyURLToFile(new URL(assetIndex.url), assetsInfo);
		AssetIndex index = new Gson().fromJson(new FileReader(assetsInfo), AssetIndex.class);
		Map<String, AssetObject> parent = index.getFileMap();

		i = 0;
		count = parent.entrySet().size();
		NotifyUtil.setProgress(-1);
		NotifyUtil.setText("Resolving %s Minecraft Assets", count);
		parent.entrySet().parallelStream().forEach(entry -> {
			AssetObject object = entry.getValue();
			String sha1 = object.getHash();
			File file = new File(ASSETS, "objects" + File.separator + sha1.substring(0, 2) + File.separator + sha1);
			NotifyUtil.setProgressAscend(i++, count);
			if (!file.exists() || !MiscUtil.checksumEquals(file, sha1)) {
				OneClientLogging.info("Downloading asset " + entry.getKey() + " from " + Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1 + " to " + file);
				try {
					FileUtils.copyURLToFile(new URL(Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1), file);
					FileUtils.copyURLToFile(new URL(Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1), file);
				} catch (IOException e) {
					OneClientLogging.error(e);
				}
			}
		});
		OneClientLogging.logger.info("Done minecraft files are all downloaded");
		InstanceManager.setInstanceInstalling(instance, false);
		NotifyUtil.clear();
	}

	public static boolean startMinecraft(Instance instance) {
		if (!MinecraftAuthController.isUserValid()) {
			MinecraftAuthController.updateGui();
			//TODO replace with a login request
			OneClientLogging.alert("You must log into minecraft to play the game!", "You are not logged in!");
			return false;
		}
		if (!MinecraftAuthController.isUserOnline()) {
			OneClientLogging.info("Launching in offline mode!");
		}

		Version versionData = getVersion(instance.getGameVersion());
		File mcJar = new File(VERSIONS, instance.getGameVersion() + ".jar");
		OneClientLogging.logger.info("Starting minecraft...");
		OneClientTracking.sendRequest("minecraft/play/" + instance.getGameVersion());
		new Thread(() -> {
			try {

				StringBuilder cpb = new StringBuilder();
				String mainClass = versionData.mainClass;
				String providedArugments = versionData.minecraftArguments;
				Optional<String> tweakClass = Optional.empty();

				if (!instance.getForgeVersion().isEmpty()) {
					for (File library : ForgeUtils.resolveForgeLibrarys(instance.getGameVersion(), instance.getForgeVersion())) {
						cpb.append(OperatingSystem.getJavaDelimiter());
						cpb.append(library.getAbsolutePath());
					}
					ForgeVersionProfile forgeVersionProfile = ForgeUtils.downloadForgeVersion(LIBRARIES, instance.getGameVersion(), instance.getForgeVersion());
					mainClass = forgeVersionProfile.mainClass;
					providedArugments = forgeVersionProfile.minecraftArguments;

					List argList = Arrays.asList(forgeVersionProfile.minecraftArguments.split(" "));
					OneClientLogging.logger.info("Using tweakclass: " + argList.get(argList.indexOf("--tweakClass") + 1).toString());
					tweakClass = Optional.of(argList.get(argList.indexOf("--tweakClass") + 1).toString()); //TODO extract from forge json
				}

				for (Version.Library library : versionData.libraries) {
					//TODO check that forge hasnt allready included the lib, as sometimes forge has a newer version of the lib than mc does. Adding the mc libs after forge is a hacky work around for it
					if (library.allowed() && library.getFile(LIBRARIES) != null) {
						cpb.append(OperatingSystem.getJavaDelimiter());
						cpb.append(library.getFile(LIBRARIES).getAbsolutePath());
					}
				}

				cpb.append(OperatingSystem.getJavaDelimiter());
				cpb.append(mcJar.getAbsolutePath());

				ArrayList<String> arguments = new ArrayList<>();
				arguments.add("java");

				arguments.add("-Djava.library.path=" + NATIVES.getAbsolutePath());

				for (String str : SettingsUtil.settings.arguments.split(" ")) {
					arguments.add(str);
				}
				arguments.add("-Xms" + SettingsUtil.settings.minecraftMinMemory + "m");
				arguments.add("-Xmx" + SettingsUtil.settings.minecraftMaxMemory + "m");

				arguments.add("-cp");
				arguments.add(cpb.toString());
				arguments.add(mainClass);

				tweakClass.ifPresent(s -> arguments.add("--tweakClass=" + s));
				//TODO improve parsing of offline/online arguments
				arguments.add("--accessToken");
				arguments.add(MinecraftAuthController.getAuthentication().getAuthenticatedToken());
				arguments.add("--uuid");
				arguments.add(MinecraftAuthController.getAuthentication().getSelectedProfile().getId().toString().replace("-", ""));
				arguments.add("--username");
				arguments.add(MinecraftAuthController.getAuthentication().getSelectedProfile().getName());
				arguments.add("--userType");
				arguments.add(MinecraftAuthController.getAuthentication().getUserType().getName());
				if (providedArugments.contains("${user_properties}")) {
					arguments.add("--userProperties");
					arguments.add((new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(MinecraftAuthController.getAuthentication().getUserProperties()));
				}

				if (providedArugments.contains("${user_properties_map}")) {
					arguments.add(new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(MinecraftAuthController.getAuthentication().getUserProperties()));
				}

				arguments.add("--version");
				arguments.add(instance.getGameVersion());
				arguments.add("--assetsDir");
				arguments.add(ASSETS.toString());
				arguments.add("--assetIndex");
				arguments.add(versionData.assetIndex.id);
				arguments.add("--gameDir");
				arguments.add(instance.getDirectory().toString());

				ProcessBuilder processBuilder = new ProcessBuilder(arguments);
				processBuilder.directory(instance.getDirectory());
				Process process = processBuilder.start();
				OneClientLogging.info("{}", arguments);
				LogController.LogTab tab = OneClientLogging.logController.getTab(instance.getName(), process);

				try {
					BufferedReader reader =
						new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					while ((line = reader.readLine()) != null) {
						tab.append(line + "\n");
					}

					BufferedReader readerErr =
						new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String lineErr;
					while ((lineErr = readerErr.readLine()) != null) {
						tab.append(lineErr + "\n");
					}
				} catch (IOException ignore) {}

			} catch (Throwable throwable) {
				OneClientLogging.error(throwable);
			}

		}).start();
		return true;
	}
}
