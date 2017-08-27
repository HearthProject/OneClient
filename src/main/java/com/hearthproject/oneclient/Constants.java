package com.hearthproject.oneclient;

import com.hearthproject.oneclient.fx.controllers.InstallLocation;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Constants {

	public static File TEMPDIR;
	public static File INSTANCEDIR;
	public static File LOGFILE;
	public static File TEMP_UPDATE;

	public static final String LIBRARIES_BASE = "https://libraries.minecraft.net/";
	public static final String RESOURCES_BASE = "http://resources.download.minecraft.net/";
	public static final String MAVEN_CENTRAL_BASE = "http://central.maven.org/maven2/";

	public static String[] INITIALIZE_DIRS = new String[] { "configs", "mods" };

	public static boolean CUSTOM_RUN = false;

	private static File RUN_DIR = null;

	public static void earlySetup(Runnable runnable) throws IOException {
		StaticSettings staticSettings = getSettings();
		if (staticSettings == null || !new File(staticSettings.installLocation).exists()) {
			Platform.runLater(() -> {
				try {
					InstallLocation.getInstallDir(file -> {
						RUN_DIR = file;
						setUpDirs();
						try {
							saveSettings(new StaticSettings(file.getAbsolutePath()));
						} catch (IOException e) {
							e.printStackTrace();
						}
						runnable.run();
						return true;
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} else {
			RUN_DIR = new File(staticSettings.installLocation);
			setUpDirs();
			runnable.run();
		}
	}

	public static void setUpDirs() {
		TEMPDIR = new File(getRunDir(), "temp");
		INSTANCEDIR = new File(getRunDir(), "instances");
		LOGFILE = new File(getRunDir(), "log.txt");
		TEMP_UPDATE = new File(getRunDir(), "temp_update.jar");
	}

	public static File getRunDir() {
		return RUN_DIR;
	}

	public static String getVersion() {
		return Constants.class.getPackage().getImplementationVersion();
	}

	public static File getDefaultDir() {
		return new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "OneClient");
	}

	public static File getInstallConfig() {
		return new File(OperatingSystem.getApplicationDataDirectory(), "settings.json");
	}

	public static StaticSettings getSettings() throws IOException {
		File config = getInstallConfig();
		if (config.exists()) {
			return JsonUtil.GSON.fromJson(FileUtils.readFileToString(config, StandardCharsets.UTF_8), StaticSettings.class);
		}
		return null;
	}

	public static void saveSettings(StaticSettings staticSettings) throws IOException {
		FileUtils.writeStringToFile(getInstallConfig(), JsonUtil.GSON.toJson(staticSettings), StandardCharsets.UTF_8);
	}

	public static class StaticSettings {
		public String installLocation;

		public StaticSettings(String installLocation) {
			this.installLocation = installLocation;
		}

		public StaticSettings() {
		}
	}

}