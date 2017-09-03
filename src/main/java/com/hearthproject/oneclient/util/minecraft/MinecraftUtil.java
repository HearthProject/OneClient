package com.hearthproject.oneclient.util.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.AssetIndex;
import com.hearthproject.oneclient.json.models.minecraft.AssetObject;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.minecraft.Version;
import com.hearthproject.oneclient.json.models.modloader.forge.ForgeVersionProfile;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.tracking.OneClientTracking;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

	public static Version downloadMcVersionData(String minecraftVersion, File versionsDir) {
		try {
			Optional<GameVersion.Version> optionalVersion = version.versions.stream().filter(versions -> versions.id.equalsIgnoreCase(minecraftVersion)).findFirst();
			if (optionalVersion.isPresent()) {
				String jsonData = IOUtils.toString(new URL(optionalVersion.get().url), StandardCharsets.UTF_8);
				FileUtils.writeStringToFile(new File(versionsDir, minecraftVersion + ".json"), jsonData, StandardCharsets.UTF_8);
				return JsonUtil.GSON.fromJson(jsonData, Version.class);
			} else {
				OneClientLogging.error(new RuntimeException("Failed downloading Minecraft json"));
				return null;
			}
		} catch (Throwable throwable) {
			OneClientLogging.error(throwable);
			return null;
		}
	}

	public static int libCount = 0;

	public static void installMinecraft(Instance instance) throws Throwable {
		InstanceManager.setInstanceInstalling(instance, true);
		NotifyUtil.setText("Installing minecraft for " + instance.getManifest().getName());
		OneClientTracking.sendRequest("minecraft/install/" + instance.getManifest().getMinecraftVersion());
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File assets = new File(mcDir, "assets");
		File versions = new File(mcDir, "versions");
		File libraries = new File(mcDir, "libraries");
		File natives = new File(mcDir, "natives");

		Version versionData = downloadMcVersionData(instance.getManifest().getMinecraftVersion(), versions);
		File mcJar = new File(versions, instance.getManifest().getMinecraftVersion() + ".jar");

		OneClientLogging.logger.info("Downloading Minecraft jar");
		if (!MiscUtil.checksumEquals(mcJar, versionData.downloads.get("client").sha1)) {
			FileUtils.copyURLToFile(new URL(versionData.downloads.get("client").url), mcJar);
		}

		OneClientLogging.logger.info("Resolving " + versionData.libraries.size() + " library's");
		libCount = 0;

		versionData.libraries.parallelStream().forEach(library -> {
			NotifyUtil.setText("Resolving library: %s", library.name);
			libCount++;
			if (library.allowed() && library.getFile(libraries) != null) {
				NotifyUtil.setProgressAscend(libCount, versionData.libraries.size());
				if (library.getFile(libraries).exists()) {
					if (MiscUtil.checksumEquals(library.getFile(libraries), library.getSha1())) {
						return;
					}
				}
				OneClientLogging.logger.info("Downloading " + library.name + " from " + library.getURL());
				try {
					FileUtils.copyURLToFile(new URL(library.getURL()), library.getFile(libraries));
				} catch (IOException e) {
					OneClientLogging.error(e);
				}
			}
		});

		versionData.libraries.stream().filter(lib -> lib.natives != null && lib.allowed()).forEach(library -> {
			OneClientLogging.logger.info("Extracting native " + library.name);
			ZipUtil.unpack(library.getFile(libraries), natives);
		});
		if (!instance.getManifest().getForge().isEmpty())
			ForgeUtils.resolveForgeLibrarys(instance.getManifest().getMinecraftVersion(), instance.getManifest().getForge());

		Version.AssetIndex assetIndex = versionData.assetIndex;
		File assetsInfo = new File(assets, "indexes" + File.separator + assetIndex.id + ".json");
		FileUtils.copyURLToFile(new URL(assetIndex.url), assetsInfo);
		AssetIndex index = new Gson().fromJson(new FileReader(assetsInfo), AssetIndex.class);
		Map<String, AssetObject> parent = index.getFileMap();

		OneClientLogging.logger.info("Resolving " + parent.entrySet().size() + " assets");
		libCount = 0;
		parent.entrySet().parallelStream().forEach(entry -> {
			AssetObject object = entry.getValue();
			String sha1 = object.getHash();
			NotifyUtil.setText("Resolving asset: %s", entry.getKey());
			NotifyUtil.setProgressAscend(libCount++, parent.entrySet().size());
			File file = new File(assets, "objects" + File.separator + sha1.substring(0, 2) + File.separator + sha1);
			if (!file.exists() || !MiscUtil.checksumEquals(file, sha1)) {
				OneClientLogging.logger.info("Downloading asset " + entry.getKey() + " from " + Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1 + " to " + file);
				try {
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

	public static boolean startMinecraft(Instance instance, String username, String password) {
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File assets = new File(mcDir, "assets");
		File versions = new File(mcDir, "versions");
		File libraries = new File(mcDir, "libraries");
		File natives = new File(mcDir, "natives");
		Version versionData = downloadMcVersionData(instance.getManifest().getMinecraftVersion(), versions);
		File mcJar = new File(versions, instance.getManifest().getMinecraftVersion() + ".jar");

		OneClientLogging.logger.info("Attempting authentication with Mojang");

		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) (new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1")).createUserAuthentication(Agent.MINECRAFT);
		auth.setUsername(username);
		auth.setPassword(password);

		try {
			auth.logIn();
		} catch (AuthenticationException e) {
			OneClientLogging.logUserError(e, "Failed to login to your minecraft account. Please check your username and password");
			return false;
		}
		OneClientLogging.logger.info("Login successful!");

		OneClientLogging.logger.info("Starting minecraft...");

		OneClientTracking.sendRequest("minecraft/play/" + instance.getManifest().getMinecraftVersion());

		new Thread(() -> {
			try {

				StringBuilder cpb = new StringBuilder();
				String mainClass = versionData.mainClass;
				String providedArugments = versionData.minecraftArguments;
				Optional<String> tweakClass = Optional.empty();

				if (!instance.getManifest().getForge().isEmpty()) {
					for (File library : ForgeUtils.resolveForgeLibrarys(instance.getManifest().getMinecraftVersion(), instance.getManifest().getForge())) {
						cpb.append(OperatingSystem.getJavaDelimiter());
						cpb.append(library.getAbsolutePath());
					}
					ForgeVersionProfile forgeVersionProfile = ForgeUtils.downloadForgeVersion(libraries, instance.getManifest().getMinecraftVersion(), instance.getManifest().getForge());
					mainClass = forgeVersionProfile.mainClass;
					providedArugments = forgeVersionProfile.minecraftArguments;

					List argList = Arrays.asList(forgeVersionProfile.minecraftArguments.split(" "));
					OneClientLogging.logger.info("Using tweakclass: " + argList.get(argList.indexOf("--tweakClass") + 1).toString());
					tweakClass = Optional.of(argList.get(argList.indexOf("--tweakClass") + 1).toString()); //TODO extract from forge json
				}

				for (Version.Library library : versionData.libraries) {
					//TODO check that forge hasnt allready included the lib, as sometimes forge has a newer version of the lib than mc does. Adding the mc libs after forge is a hacky work around for it
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

				for (String str : SettingsUtil.settings.arguments.split(" ")) {
					arguments.add(str);
				}
				arguments.add("-Xms" + SettingsUtil.settings.minecraftMinMemory + "m");
				arguments.add("-Xmx" + SettingsUtil.settings.minecraftMaxMemory + "m");

				arguments.add("-cp");
				arguments.add(cpb.toString());
				arguments.add(mainClass);

				tweakClass.ifPresent(s -> arguments.add("--tweakClass=" + s));

				arguments.add("--accessToken");
				arguments.add(auth.getAuthenticatedToken());
				arguments.add("--uuid");
				arguments.add(auth.getSelectedProfile().getId().toString().replace("-", ""));
				arguments.add("--username");
				arguments.add(auth.getSelectedProfile().getName());
				arguments.add("--userType");
				arguments.add(auth.getUserType().getName());
				if (providedArugments.contains("${user_properties}")) {
					arguments.add("--userProperties");
					arguments.add((new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));
				}

				if (providedArugments.contains("${user_properties_map}")) {
					arguments.add(new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));
				}

				arguments.add("--version");
				arguments.add(instance.getManifest().getMinecraftVersion());
				arguments.add("--assetsDir");
				arguments.add(assets.toString());
				arguments.add("--assetIndex");
				arguments.add(versionData.assetIndex.id);
				arguments.add("--gameDir");
				arguments.add(instance.getDirectory().toString());

				ProcessBuilder processBuilder = new ProcessBuilder(arguments);
				processBuilder.directory(instance.getDirectory());
				Platform.runLater(() -> OneClientLogging.logController.minecraftMenu.setDisable(false));

				Process process = processBuilder.start();
				OneClientLogging.logController.processList.add(process);

				BufferedReader reader =
					new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					OneClientLogging.logger.info(line);
				}

				BufferedReader readerErr =
					new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String lineErr;
				while ((lineErr = readerErr.readLine()) != null) {
					OneClientLogging.logger.error(lineErr);
				}

			} catch (Throwable throwable) {
				OneClientLogging.error(throwable);
			}

		}).start();
		return true;
	}
}
