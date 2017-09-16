package com.hearthproject.oneclient.util.minecraft;

import java.io.Serializable;
import java.util.Map;

public class AuthStore implements Serializable {
	public String username;
	public String password;
	public String playerName;
	public Map<String, Object> authStorage;
}
