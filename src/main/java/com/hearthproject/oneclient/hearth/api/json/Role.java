package com.hearthproject.oneclient.hearth.api.json;

public class Role {

	public static Role DEVELOPER = new Role("Dev", "http://fdn.redstone.tech/theoneclient/hearth/roles/dev.png", "Hearth Developer");
	public static Role ALPHA_TESTER = new Role("Tester", "http://fdn.redstone.tech/theoneclient/hearth/roles/alpha.png", "Alpha Tester");

	public String name;
	public String iconUrl;
	public String description;

	Role(String name, String iconUrl, String description) {
		this.name = name;
		this.iconUrl = iconUrl;
		this.description = description;
	}

	public boolean doesUserHaveRole(User user){
		if(user.roles != null){
			for(Role role : user.roles){
				if(role.name.equals(this.name)){
					return true;
				}
			}
		}
		return false;
	}
}
