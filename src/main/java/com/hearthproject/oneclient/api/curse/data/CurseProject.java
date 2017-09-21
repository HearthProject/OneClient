package com.hearthproject.oneclient.api.curse.data;

import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.json.JsonUtil;

import java.util.List;

public class CurseProject {

	public List<LatestFile> GameVersionLatestFiles;
	public String Name;
	public String PrimaryAuthorName;
	public String Summary;
	public String WebSiteURL;
	public String Id;
	public double PopularityScore;

	public boolean matchesName(String name) {
		return Name.toLowerCase().contains(name.toLowerCase());
	}

	public boolean matchesVersion(String version) {
		if (version.equals("All"))
			return true;
		return GameVersionLatestFiles.stream().map(f -> f.GameVesion).anyMatch(v -> v.equals(version));
	}

	private CurseFullProject.CurseFile[] findFiles() {
		return JsonUtil.read(Curse.getProjectFilesURL(Id), CurseFullProject.CurseFile[].class);
	}

	@Override
	public String toString() {
		return Name;
	}

	public double getPopularityScore() {
		return PopularityScore;
	}


}
