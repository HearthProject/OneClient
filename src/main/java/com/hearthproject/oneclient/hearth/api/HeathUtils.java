package com.hearthproject.oneclient.hearth.api;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class HeathUtils {

	public static boolean isPackNameValid(String name) {
		return name.matches("[A-Za-z0-9]+");
	}

	public static String getUUIDFromName(String username) throws UnirestException {
		JsonNode data = Unirest.get("https://api.mojang.com/users/profiles/minecraft/" + username).asJson().getBody();
		return data.getObject().getString("id");
	}

}
