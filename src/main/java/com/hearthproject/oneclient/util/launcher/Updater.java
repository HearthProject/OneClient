package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.LauncherUpdate;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Updater {

	public static final String updateURL = "http://hearthproject.uk/files/versions.json";

	public static Optional<String> checkForUpdate() throws IOException {
		String json = IOUtils.toString(new URL(updateURL), StandardCharsets.UTF_8);
		LauncherUpdate launcherUpdate = JsonUtil.GSON.fromJson(json, LauncherUpdate.class);
		if (Constants.getVersion() == null) {
			return Optional.empty();
		}
		if (!launcherUpdate.latestVersion.equals(Constants.getVersion())) {
			return Optional.of(launcherUpdate.latestVersion);
		}
		return Optional.empty();
	}

	public static void startUpdate() {
		try {
			String json = IOUtils.toString(new URL(updateURL), StandardCharsets.UTF_8);
			LauncherUpdate launcherUpdate = JsonUtil.GSON.fromJson(json, LauncherUpdate.class);
			FileUtils.copyURLToFile(new URL(launcherUpdate.downloadUrl), Constants.TEMP_UPDATE);
			File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			OneClientLogging.logger.error("Updating " + currentJar.getAbsolutePath() + "@" + Constants.getVersion() + " with " + Constants.TEMP_UPDATE + "@" + launcherUpdate.latestVersion);
			List<String> args = new ArrayList<>();
			args.add("java");
			args.add("-cp");
			args.add(Constants.TEMP_UPDATE.getAbsolutePath());
			args.add(Updater.class.getCanonicalName());
			args.add(currentJar.getAbsolutePath());

			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(args);
			processBuilder.start();
			System.exit(0);
		} catch (Exception e) {
			OneClientLogging.logger.error(e);
		}

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Thread.sleep(3000); //We wait a few seconds to allow the old instance to close so we can rename it without causing an exception
		File oldJar = new File(args[0]);
		File newJar = Constants.TEMP_UPDATE;
		FileUtils.moveFile(oldJar, new File(oldJar.getAbsolutePath() + ".old")); //Backup the old jar so if things go wrong it can be manually reverted
		FileUtils.copyFile(newJar, oldJar);

		List<String> newArgs = new ArrayList<>();
		newArgs.add("java");
		newArgs.add("-jar");
		newArgs.add(oldJar.getAbsolutePath()); //The old jar is now the new jar as we copyied it over
		newArgs.add("-updateSuccess"); //Added here so we can show a dialog if when the new jar is opened

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(newArgs);
		processBuilder.start();
		//Should close here
	}

}
