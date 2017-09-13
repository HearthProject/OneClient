package com.hearthproject.oneclient.hearth.json;

import java.util.List;
import java.util.UUID;

public class User {

	public String accessToken;
	public UUID UUID;
	public String username;

	public List<Role> roles;

	public User(UUID UUID) {
		this.UUID = UUID;
	}

}
