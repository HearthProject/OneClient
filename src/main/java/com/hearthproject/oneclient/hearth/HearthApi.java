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

	public static boolean login(String username, String password) throws UnirestException {
		OneClientLogging.logger.info("Logging in to hearth");
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) (new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1")).createUserAuthentication(Agent.MINECRAFT);
		auth.setUsername(username);
		auth.setPassword(password);
		try {
			auth.logIn();
		} catch (AuthenticationException e) {
			OneClientLogging.logUserError(e, "Failed to login to your minecraft account. Please check your username and password");
			return false;
		}
		HttpResponse<String> response = Unirest.get(hearthAPI + "/auth/" + auth.getAuthenticatedToken() + "/" + auth.getAuthenticationService().getClientToken()).asString();
		if (response.getStatus() == 403) {
			System.out.println(response.getBody());
			return false;
		}
		OneClientLogging.logger.info("Successfully logged into hearth");
		ClientAuthentication authentication = JsonUtil.GSON.fromJson(response.getBody(), ClientAuthentication.class);
		HearthApi.authentication = authentication;
		ContentPanes.PRIVATE_PACK_PANE.onLogin();
		return true;
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
