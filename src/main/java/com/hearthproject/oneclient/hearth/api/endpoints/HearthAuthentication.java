package com.hearthproject.oneclient.hearth.api.endpoints;

import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.hearth.api.HearthApi;
import com.hearthproject.oneclient.hearth.api.json.ClientAuthentication;
import com.hearthproject.oneclient.hearth.api.json.User;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class HearthAuthentication {

	private ClientAuthentication authentication;

	public void login(YggdrasilUserAuthentication auth) throws Exception {
		if(!auth.isLoggedIn()){
			throw new Exception("You must be logged with with mojang before you can log into hearth");
		}
		HttpResponse<String> response = Unirest.post(HearthApi.API_URL + "/auth/login/" + auth.getAuthenticatedToken() + "/" + auth.getAuthenticationService().getClientToken()).asString();
		if (response.getStatus() == 403) {
			throw new Exception(response.getBody());
		}
		OneClientLogging.logger.info("Successfully logged into hearth");
		authentication = JsonUtil.GSON.fromJson(response.getBody(), ClientAuthentication.class);
		MinecraftAuthController.setAccessToken(auth, authentication.accessToken);
		ContentPanes.PRIVATE_PACK_PANE.onLogin();
	}

	public User getUser() throws UnirestException {
		OneClientLogging.logger.info("Getting user details");
		if (authentication == null || authentication.accessToken.isEmpty()) {
			return null;
		}
		HttpResponse<String> response = Unirest.get(HearthApi.API_URL + "/" + authentication.accessToken + "/user/data").asString();
		if (response.getStatus() == 403) {
			OneClientLogging.logger.error("Failed to get client permissions");
			return null;
		}
		System.out.println(response.getBody());
		User user = JsonUtil.GSON.fromJson(response.getBody(), User.class);
		return user;
	}

	public ClientAuthentication getAuthentication() {
		return authentication;
	}
}
