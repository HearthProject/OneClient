package com.hearthproject.oneclient.hearth.api;

import com.google.gson.Gson;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.hearth.api.endpoints.HearthAuthentication;
import com.hearthproject.oneclient.hearth.api.endpoints.HearthPrivatePacks;
import com.hearthproject.oneclient.hearth.api.json.ClientAuthentication;
import com.hearthproject.oneclient.hearth.api.json.User;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class HearthApi {

	//Use this to enable it
	public static boolean enable = true;
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

	public static GetRequest authenticatedGetRequest(String endpoint){
		return Unirest.get(API_URL + "/" + getHearthAuthentication().getAuthentication().accessToken + endpoint);
	}

	public static HttpRequestWithBody authicatedPostRequset(String endpoint){
		return Unirest.post(API_URL + "/" + getHearthAuthentication().getAuthentication().accessToken + endpoint);
	}

}
