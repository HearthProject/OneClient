package com.hearthproject.oneclient.hearth.api.json;

//This is data that others users can know about the user provided
public class UserData {

	public String uuid;
	public String username;

	public UserData(String uuid, String username) {
		this.uuid = uuid;
		this.username = username;
	}

	public UserData(User user){
		this.uuid = user.uuid;
		this.username = user.username;
	}

}
