package com.hearthproject.oneclient.util.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.HearthInstance;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.LogController;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.json.JsonUtil;
import com.hearthproject.oneclient.util.json.models.minecraft.AssetIndex;
import com.hearthproject.oneclient.util.json.models.minecraft.AssetObject;
import com.hearthproject.oneclient.util.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.util.json.models.minecraft.Version;
import com.hearthproject.oneclient.util.json.models.modloader.forge.ForgeVersionProfile;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.tracking.OneClientTracking;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MinecraftUtil {

	// http://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.jar
	// https://launcher.mojang.com/download/MinecraftInstaller.msi

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
	}

	private static String parseVersionManifest() throws UnknownHostException {
		if (!VERSION_MANIFEST.exists()) {
			SplashScreen.updateProgess("Downloading minecraft version json", 20);
			try {
				FileUtil.downloadFromURL(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), VERSION_MANIFEST);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		try {
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

	public static GameVersion getGameVersions() {
		if (version == null)
			parseGameVersions();
		return version;
	}

	public static GameVersion.VersionData getVersionData(String minecraftVersion) {
		return version.get(versions -> versions.id.equalsIgnoreCase(minecraftVersion)).findFirst().orElse(null);
	}

	public static Version parseVersionData(String minecraftVersion) {
		return getVersionData(minecraftVersion).getData();
	}

	public static int i = 0, count;

	public static void installMinecraft(HearthInstance instance) throws Throwable {
		//TODO
		//InstanceManager.setInstanceInstalling(instance, true);
		NotifyUtil.setText("Installing minecraft for " + instance.getName());
		OneClientTracking.sendRequest("minecraft/install/" + instance.getGameVersion());

		Version versionData = parseVersionData(instance.getGameVersion());
		File mcJar = new File(VERSIONS, instance.getGameVersion() + ".jar");

		OneClientLogging.logger.info("Downloading Minecraft jar");
		if (!MiscUtil.checksumEquals(mcJar, versionData.downloads.get("client").sha1)) {
			FileUtils.copyURLToFile(new URL(versionData.downloads.get("client").url), mcJar);
		}

		i = 0;
		count = versionData.libraries.size();
		NotifyUtil.setText("Resolving %s Libraries", count);
		OneClientLogging.info("{}", versionData.libraries);
		versionData.libraries.stream().forEach(library -> {
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
		//TODO
		//		InstanceManager.setInstanceInstalling(instance, false);
		NotifyUtil.clear();
	}

	//functionalize this more.
	public static boolean startMinecraft(HearthInstance instance, String username, String password) {
		Version versionData = parseVersionData(instance.getGameVersion());
		File mcJar = new File(VERSIONS, instance.getGameVersion() + ".jar");

		OneClientLogging.logger.info("Attempting authentication with Mojang");

		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) (new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1")).createUserAuthentication(Agent.MINECRAFT);
		auth.setUsername(username);
		auth.setPassword(password);
		boolean isOffline = false;
		try {
			auth.logIn();
		} catch (AuthenticationException e) {
			isOffline = shouldLaunchOffline("Failed to login to your minecraft account. Would you like to launch offline?");
			if (!isOffline) {
				return false;
			}
		}
		OneClientLogging.logger.info("Login successful!");

		OneClientLogging.logger.info("Starting minecraft...");

		OneClientTracking.sendRequest("minecraft/play/" + instance.getGameVersion());
		final boolean offline = isOffline;
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
				if (!offline) {
					arguments.add(auth.getAuthenticatedToken());
				}
				arguments.add("--uuid");
				if (!offline) {
					arguments.add(auth.getSelectedProfile().getId().toString().replace("-", ""));
				}
				arguments.add("--username");
				arguments.add(auth.getSelectedProfile().getName());
				arguments.add("--userType");
				if (!offline) {
					arguments.add(auth.getUserType().getName());
				}

				if (providedArugments.contains("${user_properties}")) {

					arguments.add("--userProperties");
					arguments.add((new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));
				}

				if (providedArugments.contains("${user_properties_map}")) {
					arguments.add(new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));
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

	public static boolean shouldLaunchOffline(String title) {
		Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.YES, ButtonType.NO);
		alert.setTitle("Error!");
		alert.setHeaderText(title);
		alert.getButtonTypes().addAll();
		return alert.showAndWait().map(b -> b == ButtonType.YES).orElse(false);
	}

}
