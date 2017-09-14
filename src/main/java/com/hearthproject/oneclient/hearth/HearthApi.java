package com.hearthproject.oneclient.hearth;

import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.hearth.json.ClientAuthentication;
import com.hearthproject.oneclient.hearth.json.User;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class HearthApi {

	//Use this to enable it
	public static boolean enable = false;
	public static String hearthAPI = "http://localhost:4567/v1";

	private static ClientAuthentication authentication;

	public static void login(YggdrasilUserAuthentication auth) throws Exception {
		if(!auth.isLoggedIn()){
			throw new Exception("You must be logged with with mojang before you can log into hearth");
		}
		HttpResponse<String> response = Unirest.post(hearthAPI + "/auth/login/" + auth.getAuthenticatedToken() + "/" + auth.getAuthenticationService().getClientToken()).asString();
		if (response.getStatus() == 403) {
			throw new Exception(response.getBody());
		}
		OneClientLogging.logger.info("Successfully logged into hearth");
		ClientAuthentication authentication = JsonUtil.GSON.fromJson(response.getBody(), ClientAuthentication.class);
		HearthApi.authentication = authentication;
		MinecraftAuthController.setAccessToken(auth, authentication.accessToken);
		ContentPanes.PRIVATE_PACK_PANE.onLogin();
	}

	public static User getUser() throws UnirestException {
		OneClientLogging.logger.info("Getting user details");
		if (authentication == null || authentication.accessToken.isEmpty()) {
			return null;
		}
		HttpResponse<String> response = Unirest.get(hearthAPI + "/" + authentication.accessToken + "/user/data").asString();
		if (response.getStatus() == 403) {
			OneClientLogging.logger.error("Failed to get client permissions");
			return null;
		}
		System.out.println(response.getBody());
		User user = JsonUtil.GSON.fromJson(response.getBody(), User.class);
		return user;
	}

	public static ClientAuthentication getAuthentication() {
		return authentication;
	}

}
