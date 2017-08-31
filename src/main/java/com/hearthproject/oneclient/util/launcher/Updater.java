package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.LauncherUpdate;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.filechooser.FileSystemView;
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
		File oldJar = new File(new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "OneClient"), "temp_update.jar");
		if (oldJar.exists()) {
			oldJar.delete();
		}
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

	public static File getTempFile(boolean startUpdate) {
		if (startUpdate) {
			return new File(OperatingSystem.getApplicationDataDirectory(), "temp_update.jar");
		} else {
			//This is to allow updating from versions before the custom dir was added
			File oldJar = new File(new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "OneClient"), "temp_update.jar");
			if (oldJar.exists()) {
				return oldJar;
			}
			return new File(OperatingSystem.getApplicationDataDirectory(), "temp_update.jar");
		}
	}

	public static void startUpdate() {
		try {
			String json = IOUtils.toString(new URL(updateURL), StandardCharsets.UTF_8);
			LauncherUpdate launcherUpdate = JsonUtil.GSON.fromJson(json, LauncherUpdate.class);
			FileUtils.copyURLToFile(new URL(launcherUpdate.downloadUrl), getTempFile(true));
			File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			OneClientLogging.logger.error("Updating " + currentJar.getAbsolutePath() + "@" + Constants.getVersion() + " with " + getTempFile(true) + "@" + launcherUpdate.latestVersion);
			List<String> args = new ArrayList<>();
			args.add("java");
			args.add("-cp");
			args.add(getTempFile(true).getAbsolutePath());
			args.add(Updater.class.getCanonicalName());
			args.add(currentJar.getAbsolutePath());

			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(args);
			processBuilder.start();
			System.exit(0);
		} catch (Exception e) {
			OneClientLogging.error(e);
		}

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Thread.sleep(3000); //We wait a few seconds to allow the old instance to close so we can rename it without causing an exception
		File oldJar = new File(args[0]);
		File newJar = getTempFile(false);
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
