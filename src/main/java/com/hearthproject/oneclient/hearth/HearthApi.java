package com.hearthproject.oneclient.hearth;

import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.hearth.json.ClientAuthentication;
import com.hearthproject.oneclient.hearth.json.ClientPermissions;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import java.net.Proxy;

public class HearthApi {

	//Use this to enable it
	public static boolean enable = false;
	public static String hearthAPI = "http://localhost:4567";

	private static ClientAuthentication authentication;

	public static void login(YggdrasilUserAuthentication auth) throws Exception {
		OneClientLogging.logger.info("Logging in to hearth");
		if(!auth.isLoggedIn()){
			throw new Exception("You must be logged with with mojang before you can log into hearth");
		}
		HttpResponse<String> response = Unirest.get(hearthAPI + "/auth/" + auth.getAuthenticatedToken() + "/" + auth.getAuthenticationService().getClientToken()).asString();
		if (response.getStatus() == 403) {
			throw new Exception(response.getBody());
		}
		OneClientLogging.logger.info("Successfully logged into hearth");
		ClientAuthentication authentication = JsonUtil.GSON.fromJson(response.getBody(), ClientAuthentication.class);
		HearthApi.authentication = authentication;
		ContentPanes.PRIVATE_PACK_PANE.onLogin();
	}

	public static ClientPermissions getClientPermissions() throws UnirestException {
		OneClientLogging.logger.info("Checking client permissions");
		if (authentication == null || authentication.accessToken.isEmpty()) {
			return null;
		}
		HttpResponse<String> response = Unirest.get(hearthAPI + "/client/" + authentication.accessToken + "/permissions").asString();
		if (response.getStatus() == 403) {
			OneClientLogging.logger.error("Failed to get client permissions");
			return null;
		}
		ClientPermissions clientPermissions = JsonUtil.GSON.fromJson(response.getBody(), ClientPermissions.class);
		return clientPermissions;
	}

	public static ClientAuthentication getAuthentication() {
		return authentication;
	}

}
