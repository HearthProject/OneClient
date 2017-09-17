package com.hearthproject.oneclient.hearth.api.endpoints;

import com.hearthproject.oneclient.hearth.api.HearthApi;
import com.hearthproject.oneclient.hearth.api.json.packs.ModPack;
import com.mashape.unirest.http.HttpResponse;
import org.json.JSONObject;

public class HearthPrivatePacks {

	public boolean isValid(String name) throws Exception {
		if(name.isEmpty()){
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
		HttpResponse<String> response = HearthApi.authicatedPostRequset("/packs/new")
			.header("Content-Type", "application/json")
			.body(jsonObject.toString())
			.asString();

		String body = response.getBody();

		ModPack modPack = HearthApi.GSON.fromJson(body, ModPack.class);
		return modPack;
	}

}
