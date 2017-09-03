package com.hearthproject.oneclient.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void save(File file, String json) {
		try {
			FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
	}

	public static <T> T read(File file, Class<T> object) {
		if (!file.exists()) {
			OneClientLogging.error(new FileNotFoundException("File not found:" + file));
		}
		try {
			return GSON.fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), object);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
