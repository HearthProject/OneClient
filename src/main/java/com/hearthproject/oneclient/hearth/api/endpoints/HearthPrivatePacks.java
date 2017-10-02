package com.hearthproject.oneclient.hearth.api.endpoints;

import com.google.gson.reflect.TypeToken;
import com.hearthproject.oneclient.hearth.api.HearthApi;
import com.hearthproject.oneclient.hearth.api.json.packs.ModPack;
import com.hearthproject.oneclient.hearth.api.json.packs.PackUpload;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HearthPrivatePacks {

	public boolean isValid(String name) throws Exception {
		if (name.isEmpty()) {
			return false;
		}
		HttpResponse<String> response = HearthApi.authenticatedGetRequest("/packs/" + name + "/valid").asString();
		JSONObject jsonObject = new JSONObject(response.getBody());
		return jsonObject.getBoolean("value");
	}

	//Creates a new pack, and returns the modpack info that the server provided
	public ModPack createNewPack(String name, String description) throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", name);
		jsonObject.put("description", description);
		HttpResponse<String> response = HearthApi.authenticatedPostRequest("/packs/new")
			.header("Content-Type", "application/json")
			.body(jsonObject.toString())
			.asString();

		String body = response.getBody();

		ModPack modPack = HearthApi.GSON.fromJson(body, ModPack.class);
		return modPack;
	}

	public List<ModPack> getPacks() throws UnirestException {
		HttpResponse<String> response = HearthApi.authenticatedGetRequest("/packs/list")
			.header("Content-Type", "application/json")
			.asString();

		System.out.println(response.getBody());
		List<ModPack> packs = HearthApi.GSON.fromJson(response.getBody(), new TypeToken<ArrayList<ModPack>>() {}.getType());
		System.out.println(packs);
		return packs;
	}

	public List<ModPack> getAdminPacks() throws UnirestException {
		HttpResponse<String> response = HearthApi.authenticatedGetRequest("/packs/adminList")
			.header("Content-Type", "application/json")
			.asString();

		List<ModPack> packs = HearthApi.GSON.fromJson(response.getBody(), new TypeToken<ArrayList<ModPack>>() {}.getType());
		return packs;
	}

	//TODO add someway of tracking the progress of the file upload
	public ModPack.ModPackVersion uploadNewPack(PackUpload packUpload, File file) throws UnirestException {
		HttpResponse<String> response = HearthApi.authenticatedPostRequest("/packs/" + packUpload.modpack + "/admin/newVersion")
			.header("Content-Type", "application/json")
			.field("file", file)
			.field("json", HearthApi.GSON.toJson(packUpload))
			.asString();
		ModPack.ModPackVersion modPackVersion = HearthApi.GSON.fromJson(response.getBody(), ModPack.ModPackVersion.class);
		return modPackVersion;
	}
}
