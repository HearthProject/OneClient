package com.hearthproject.oneclient.api.modpack.curse.data;

import com.hearthproject.oneclient.api.modpack.curse.Curse;

import java.util.List;

public class CurseProject {

	public List<LatestFile> GameVersionLatestFiles;
	public String Name;
	public String PrimaryAuthorName;
	public String Summary;
	public String WebSiteURL;
	public String Id;
	public double PopularityScore;

	public boolean matchesAuthor(String author) { return PrimaryAuthorName.equalsIgnoreCase(author); }

	public boolean matchesName(String name) {
		return Name.toLowerCase().contains(name.toLowerCase());
	}

	public boolean matchesVersion(String version) {
		if (version.equals("All"))
			return true;

		return GameVersionLatestFiles.stream().map(LatestFile::getGameVersion).anyMatch(v -> v.equals(Curse.formatVersion(version)));
	}


	@Override
	public String toString() {
		return Name;
	}

	public double getPopularityScore() {
		return PopularityScore;
	}

}
