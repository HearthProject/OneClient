package com.hearthproject.oneclient.util.minecraft;

import java.io.Serializable;

public class AuthStore implements Serializable {
	public String username;
	public String password;
	public String accessToken;
	public String clientToken;
	public String playerName;
	//TODO store the auth token for offline launches
}
