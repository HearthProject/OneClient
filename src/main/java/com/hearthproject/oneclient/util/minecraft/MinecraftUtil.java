package com.hearthproject.oneclient.util.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.forge.ForgeVersionProfile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.AssetIndex;
import com.hearthproject.oneclient.json.models.minecraft.AssetObject;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.minecraft.Version;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
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
				OneClientLogging.log(new RuntimeException("Failed downloading Minecraft json"));
				return null;
			}
		} catch (Throwable throwable) {
			OneClientLogging.log(throwable);
			return null;
		}
	}

	public static void installMinecraft(Instance instance) throws Throwable {
		OneClientLogging.log("Installing minecraft for " + instance.name);
		Platform.runLater(() -> {
			try {
				InstallingController.showInstaller();
				InstallingController.controller.setTitleText("Downloading Minecraft " + instance.minecraftVersion);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		OneClientTracking.sendRequest("minecraft/install/" + instance.minecraftVersion);
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File assets = new File(mcDir, "assets");
		File versions = new File(mcDir, "versions");
		File libraries = new File(mcDir, "libraries");
		File natives = new File(mcDir, "natives");

		Version versionData = downloadMcVersionData(instance.minecraftVersion, versions);
		File mcJar = new File(versions, instance.minecraftVersion + ".jar");

		OneClientLogging.log("Downloading Minecraft jar");
		if (!MiscUtil.checksumEquals(mcJar, versionData.downloads.get("client").sha1)) {
			FileUtils.copyURLToFile(new URL(versionData.downloads.get("client").url), mcJar);
		}

		OneClientLogging.log("Resolving " + versionData.libraries.size() + " library's");
		int i = 0;
		for (Version.Library library : versionData.libraries) {
			InstallingController.controller.setDetailText("Resolving library " + library.name);
			InstallingController.controller.setProgress(i++, versionData.libraries.size());
			if (library.allowed() && library.getFile(libraries) != null) {
				if (library.getFile(libraries).exists()) {
					if (MiscUtil.checksumEquals(library.getFile(libraries), library.getSha1())) {
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

		if (instance.modLoader.equalsIgnoreCase("forge") && !instance.modLoaderVersion.isEmpty()) {
			ForgeUtils.resolveForgeLibrarys(instance.minecraftVersion + "-" + instance.modLoaderVersion);
		}

		Version.AssetIndex assetIndex = versionData.assetIndex;
		File assetsInfo = new File(assets, "indexes" + File.separator + assetIndex.id + ".json");
		FileUtils.copyURLToFile(new URL(assetIndex.url), assetsInfo);
		AssetIndex index = new Gson().fromJson(new FileReader(assetsInfo), AssetIndex.class);
		Map<String, AssetObject> parent = index.getFileMap();

		OneClientLogging.log("Resolving " + parent.entrySet().size() + " assets");
		i = 0;
		for (Map.Entry<String, AssetObject> entry : parent.entrySet()) {
			AssetObject object = entry.getValue();
			String sha1 = object.getHash();
			InstallingController.controller.setDetailText("Resolving asset " + entry.getKey());
			InstallingController.controller.setProgress(i++, parent.entrySet().size());
			File file = new File(assets, "objects" + File.separator + sha1.substring(0, 2) + File.separator + sha1);
			if (!file.exists() || !MiscUtil.checksumEquals(file, sha1)) {
				OneClientLogging.log("Downloading asset " + entry.getKey() + " from " + Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1 + " to " + file);
				FileUtils.copyURLToFile(new URL(Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1), file);
			}
		}
		OneClientLogging.log("Done minecraft files are all downloaded");
		InstallingController.close();
	}

	public static boolean startMinecraft(Instance instance, String username, String password) {
		File mcDir = new File(Constants.getRunDir(), "minecraft");
		File assets = new File(mcDir, "assets");
		File versions = new File(mcDir, "versions");
		File libraries = new File(mcDir, "libraries");
		File natives = new File(mcDir, "natives");
		Version versionData = downloadMcVersionData(instance.minecraftVersion, versions);
		File mcJar = new File(versions, instance.minecraftVersion + ".jar");

		OneClientLogging.log("Attempting authentication with Mojang");

		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) (new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1")).createUserAuthentication(Agent.MINECRAFT);
		auth.setUsername(username);
		auth.setPassword(password);

		try {
			auth.logIn();
		} catch (AuthenticationException e) {
			OneClientLogging.logUserError(e, "Failed to login to your minecraft account. Please check your username and password");
			return false;
		}
		OneClientLogging.log("Login successful!");

		OneClientLogging.log("Starting minecraft...");

		OneClientTracking.sendRequest("minecraft/play/" + instance.minecraftVersion);

		new Thread(() -> {
			try {

				StringBuilder cpb = new StringBuilder();
				String mainClass = versionData.mainClass;
				Optional<String> tweakClass = Optional.empty();

				if (instance.modLoader.equalsIgnoreCase("forge") && !instance.modLoaderVersion.isEmpty()) {
					for (File library : ForgeUtils.resolveForgeLibrarys(instance.minecraftVersion + "-" + instance.modLoaderVersion)) {
						cpb.append(OperatingSystem.getJavaDelimiter());
						cpb.append(library.getAbsolutePath());
					}
					ForgeVersionProfile forgeVersionProfile = ForgeUtils.downloadForgeVersion(libraries, instance.minecraftVersion + "-" + instance.modLoaderVersion);
					mainClass = forgeVersionProfile.mainClass;

					List argList = Arrays.asList(forgeVersionProfile.minecraftArguments.split(" "));
					OneClientLogging.log("Using tweakclass: " + argList.get(argList.indexOf("--tweakClass") + 1).toString());
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

				System.out.println(cpb.toString());

				ArrayList<String> arguments = new ArrayList<>();
				arguments.add("java");

				arguments.add("-Djava.library.path=" + natives.getAbsolutePath());

				arguments.add("-cp");
				arguments.add(cpb.toString());
				arguments.add(mainClass);

				tweakClass.ifPresent(s -> arguments.add("--tweakClass=" + s));

				arguments.add("--accessToken=" + auth.getAuthenticatedToken());
				arguments.add("--uuid=" + auth.getSelectedProfile().getId().toString().replace("-", ""));
				arguments.add("--username=" + auth.getSelectedProfile().getName());
				arguments.add("--userType=" + auth.getUserType().getName());
				arguments.add("--userProperties=" + (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));

				arguments.add("--version=" + instance.minecraftVersion);
				arguments.add("--assetsDir=" + assets);
				arguments.add("--assetIndex=" + versionData.assetIndex.id);
				arguments.add("--gameDir=" + new File(Constants.INSTANCEDIR, instance.name));

				ProcessBuilder processBuilder = new ProcessBuilder(arguments);
				processBuilder.directory(new File(Constants.INSTANCEDIR, instance.name));
				Platform.runLater(() -> OneClientLogging.logController.minecraftMenu.setDisable(false));

				Process process = processBuilder.start();
				OneClientLogging.logController.processList.add(process);

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
		return true;
	}
}
