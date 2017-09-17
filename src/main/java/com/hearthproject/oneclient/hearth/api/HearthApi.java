package com.hearthproject.oneclient.hearth.api;

import com.google.gson.Gson;
import com.hearthproject.oneclient.hearth.api.endpoints.HearthAuthentication;
import com.hearthproject.oneclient.hearth.api.endpoints.HearthPrivatePacks;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class HearthApi {

	//Use this to enable it
	public static boolean enable = false;
	public static String API_URL = "http://localhost:4567/v1";

	public static Gson GSON = new Gson();

	private static HearthAuthentication hearthAuthentication = new HearthAuthentication();
	private static HearthPrivatePacks hearthPrivatePacks = new HearthPrivatePacks();

	public static HearthAuthentication getHearthAuthentication() {
		return hearthAuthentication;
	}

	public static HearthPrivatePacks getHearthPrivatePacks() {
		return hearthPrivatePacks;
	}

	public static GetRequest authenticatedGetRequest(String endpoint) {
		return Unirest.get(API_URL + "/" + getHearthAuthentication().getAuthentication().accessToken + endpoint);
	}

	public static HttpRequestWithBody authicatedPostRequset(String endpoint) {
		return Unirest.post(API_URL + "/" + getHearthAuthentication().getAuthentication().accessToken + endpoint);
	}

}
