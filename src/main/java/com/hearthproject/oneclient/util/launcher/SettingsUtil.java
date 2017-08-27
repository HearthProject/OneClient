package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SettingsUtil {
	public static File settingsFile;
	public static LauncherSettings settings;

	public static void init() {
		settingsFile = new File(Constants.getRunDir(), "settings.json");
		try {
			load();
		} catch (IOException e) {
			OneClientLogging.logger.error(e);
		}
	}

	public static void load() throws IOException {
		if (!settingsFile.exists()) {
			saveSetting();
		}
		settings = JsonUtil.GSON.fromJson(FileUtils.readFileToString(settingsFile, StandardCharsets.UTF_8), LauncherSettings.class);
	}

	public static void saveSetting() throws IOException {
		if (settings == null) {
			settings = new LauncherSettings();
		}
		FileUtils.writeStringToFile(settingsFile, JsonUtil.GSON.toJson(settings), StandardCharsets.UTF_8);
	}
}