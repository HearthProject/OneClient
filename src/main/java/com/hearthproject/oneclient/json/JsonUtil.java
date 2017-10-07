package com.hearthproject.oneclient.json;

import com.google.gson.*;
import com.hearthproject.oneclient.api.modpack.ModInstaller;
import com.hearthproject.oneclient.api.modpack.ModpackInstaller;
import com.hearthproject.oneclient.api.modpack.PackType;
import com.hearthproject.oneclient.api.modpack.curse.CurseInstaller;
import com.hearthproject.oneclient.api.modpack.curse.CurseModInstaller;
import com.hearthproject.oneclient.api.modpack.curse.data.FileData;
import com.hearthproject.oneclient.util.files.FileHash;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import io.gsonfire.GsonFireBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonUtil {

	public static final Gson GSON = new GsonFireBuilder().registerTypeSelector(ModpackInstaller.class, readElement -> {
		String type = readElement.getAsJsonObject().get("type").getAsString();
		if (type.equals(PackType.CURSE.name())) {
			return CurseInstaller.class;
		}
		return ModpackInstaller.class;
	}).createGsonBuilder()
		.registerTypeAdapter(ModInstaller.class, new ModInstallDeserialize())
		.registerTypeAdapter(ModInstaller.class, new ModInstallSerialize())
		.registerTypeAdapterFactory(new JavaFxPropertyTypeAdapterFactory()).registerTypeAdapter(ObservableList.class, new ObservableListDeserializer<>()).setPrettyPrinting().create();

	static class ObservableListDeserializer<T> implements JsonDeserializer<ObservableList<T>> {

		@Override
		public ObservableList<T> deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
			Gson gson = new Gson();
			List<T> tasks = gson.fromJson(json.getAsJsonArray().toString(), type);
			return FXCollections.observableArrayList(tasks);
		}

	}

	static class ModInstallDeserialize implements JsonDeserializer<ModInstaller> {
		@Override
		public ModInstaller deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = (JsonObject) json;
			if (object.get("type").getAsString().equals("CURSE")) {
				CurseModInstaller installer = new CurseModInstaller();
				installer.setName(object.get("name").getAsString());
				installer.setHash(GSON.fromJson(object.getAsJsonObject("hash"), FileHash.class));
				installer.setFileData(GSON.fromJson(object.getAsJsonObject("fileData"), FileData.class));
			}
			return GSON.fromJson(object, ModInstaller.class);
		}
	}

	static class ModInstallSerialize implements JsonSerializer<ModInstaller> {

		@Override
		public JsonElement serialize(ModInstaller src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("name", src.getName());
			object.addProperty("type", src.getType().name());
			object.add("hash", GSON.toJsonTree(src.getHash()));
			if (src instanceof CurseModInstaller) {
				object.add("data", GSON.toJsonTree(((CurseModInstaller) src).getFileData()));
			}
			return object;
		}
	}

	public static void save(File file, String json) {
		try {
			FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
	}

	public static <T> T read(File file, Class<T> object) {
		if (!file.exists())
			return null;
		try {
			return GSON.fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), object);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static <T> T read(URL url, Class<T> object) {
		try {
			InputStreamReader reader = new InputStreamReader(url.openStream());
			return GSON.fromJson(reader, object);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}

		return null;
	}

	public static <T> T read(URL url, Type type) {
		try {
			InputStreamReader reader = new InputStreamReader(url.openStream());
			return GSON.fromJson(reader, type);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}

		return null;
	}
}
