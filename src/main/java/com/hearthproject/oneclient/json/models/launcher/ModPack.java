package com.hearthproject.oneclient.json.models.launcher;

public class ModPack {
	public String name;
	public String authors;
	public String description;
	public String iconUrl;

	public ModPack(String name, String authors, String description, String iconUrl) {
		this.name = name;
		this.authors = authors;
		this.description = description;
		this.iconUrl = iconUrl;
	}

	public ModPack() {
	}
}
