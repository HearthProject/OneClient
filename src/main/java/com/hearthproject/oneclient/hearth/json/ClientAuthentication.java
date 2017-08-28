package com.hearthproject.oneclient.hearth.json;

public class ClientAuthentication {
	public String username;
	public String id;
	public String accessToken;
	public String clientToken;

	public ClientAuthentication(String username, String id, String accessToken, String clientToken) {
		this.username = username;
		this.id = id;
		this.accessToken = accessToken;
		this.clientToken = clientToken;
	}

	public ClientAuthentication() {
	}
}
