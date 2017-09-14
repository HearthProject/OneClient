package com.hearthproject.oneclient.hearth.json;

public enum  Role {

	DEVELOPER("Dev", "", "Hearth Developer"),
	ALPHATESTER("Tester", "", "Alpha Tester");

	String name;
	String iconUrl;
	String description;

	Role(String name, String iconUrl, String description) {
		this.name = name;
		this.iconUrl = iconUrl;
		this.description = description;
	}

	public boolean doesUserHaveRole(User user){
		return user.roles != null && user.roles.contains(this);
	}
}
