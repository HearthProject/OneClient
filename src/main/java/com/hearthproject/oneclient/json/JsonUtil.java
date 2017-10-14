package com.hearthproject.oneclient.json;

import com.google.gson.*;
import com.hearthproject.oneclient.api.base.FileData;
import com.hearthproject.oneclient.api.base.ModInstaller;
import com.hearthproject.oneclient.api.base.ModpackInstaller;
import com.hearthproject.oneclient.api.base.PackType;
import com.hearthproject.oneclient.api.modpack.curse.CurseDownloader;
import com.hearthproject.oneclient.api.modpack.curse.CurseFileData;
import com.hearthproject.oneclient.api.modpack.curse.CurseModInstaller;
import com.hearthproject.oneclient.api.modpack.manual.ManualModInstaller;
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

public class JsonUtil {

    public static final Gson GSON = new GsonFireBuilder().registerTypeSelector(ModpackInstaller.class, readElement -> {
        String type = readElement.getAsJsonObject().get("type").getAsString();
        if (type.equals(PackType.CURSE.name())) {
            return CurseDownloader.class;
        }
        return ModpackInstaller.class;
    })
            .createGsonBuilder()
            .registerTypeHierarchyAdapter(ModInstaller.class, new ModInstallerAdapter())
            .registerTypeAdapterFactory(new JavaFxPropertyTypeAdapterFactory()).registerTypeAdapter(ObservableList.class, new ObservableListCreator()).setPrettyPrinting().create();

    static class ObservableListCreator implements InstanceCreator<ObservableList<?>> {
        public ObservableList<?> createInstance(Type type) {
            // No need to use a parametrized list since the actual instance will have the raw type anyway.
            return FXCollections.observableArrayList();
        }
    }

    static class ModInstallerAdapter implements JsonDeserializer<ModInstaller>, JsonSerializer<ModInstaller> {
        @Override
        public ModInstaller deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = (JsonObject) json;

            PackType type = PackType.byName(object.get("type").getAsString());
            ModInstaller installer = null;
            switch (type) {
                case CURSE:
                    CurseFileData data = GSON.fromJson(object.getAsJsonObject("data"), CurseFileData.class);
                    installer = new CurseModInstaller(data.getFileID(), data.getFileID());
                    break;
                case MANUAL:
                    installer = new ManualModInstaller(new FileData());
                    break;
            }
            if (installer != null)
                installer.setName(object.get("name").getAsString());
            return installer;
        }

        @Override
        public JsonElement serialize(ModInstaller src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("name", src.getName());
            object.addProperty("type", src.getType().name());
            object.add("data", GSON.toJsonTree(src.getData()));
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
