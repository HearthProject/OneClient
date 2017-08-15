package com.hearthproject.oneclient.json.models.launcher;

import com.hearthproject.oneclient.json.models.curse.ModPacks;

public class ModPack {
	public String name;
	public String authors;
	public String description;
	public String iconUrl;

	public ModPack() {
	}

	public ModPack(ModPacks.CursePack cursePack){
		this.name = cursePack.Name;
		this.authors = cursePack.PrimaryAuthorName;
		this.description = cursePack.Summary;
		this.iconUrl = cursePack.Attachments.get(0).Url;
	}
}
