package com.hearthproject.oneclient.api;

import com.google.gson.*;
import com.hearthproject.oneclient.json.JsonUtil;

import java.lang.reflect.Type;

public class ModInstaller implements IInstallable {

	protected PackType type;
	protected transient Instance instance;

	public ModInstaller(PackType type) {
		this.type = type;
	}

	public void install(Instance instance) {}

	public void update(Instance instance) {}

	@Override
	public PackType getType() {
		return type;
	}

	public static class ModInstallerAdapter implements JsonSerializer<ModInstaller>, JsonDeserializer<ModInstaller> {

		@Override
		public ModInstaller deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			PackType type = PackType.byName(object.get("type").getAsString());
			return new ModInstaller(type);
		}

		@Override
		public JsonElement serialize(ModInstaller src, Type typeOfSrc, JsonSerializationContext context) {
			return JsonUtil.GSON.toJsonTree(src);
		}
	}

}
