package com.hearthproject.oneclient.api.curse.data;

import com.hearthproject.oneclient.api.curse.CurseMod;

import java.util.List;

public class CurseModData {
	public List<LatestFile> GameVersionLatestFiles;
	public String Name;
	public String PrimaryAuthorName;
	public String Summary;
	public String WebSiteURL;
	public String Id;

	public boolean matchesName(String name) {
		return Name.toLowerCase().contains(name.toLowerCase());
	}

	public boolean matchesVersion(String version) {
		if (version.equals("All"))
			return true;
		return GameVersionLatestFiles.stream().map(f -> f.GameVesion).anyMatch(v -> v.equals(version));
	}

	public String getName() {
		return Name;
	}

	public CurseMod create() {
		return new CurseMod(this);
	}

}
