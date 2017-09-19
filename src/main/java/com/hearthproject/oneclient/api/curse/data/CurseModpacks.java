package com.hearthproject.oneclient.api.curse.data;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.json.JsonUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CurseModpacks extends HashMap<String, CurseModpacks.CurseModpack> {

	@Override
	public CurseModpack put(String key, CurseModpack value) {
		value.Id = key;
		return super.put(key, value);
	}

	public class CurseModpack {

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

		private CurseProject.CurseFile[] findFiles() {
			return JsonUtil.read(Curse.getProjectFilesURL(Id), CurseProject.CurseFile[].class);
		}

		@Override
		public String toString() {
			return Name;
		}

		public double getPopularityScore() {
			return PopularityScore;
		}
	}

	public class LatestFile {
		public String FileType;
		public String GameVesion;
		public String ProjectFileID;
	}

	private List<Map.Entry<String, CurseModpacks.CurseModpack>> getEntries() {
		List<Map.Entry<String, CurseModpacks.CurseModpack>> entries = Lists.newArrayList(this.entrySet());
		entries.sort(Comparator.comparing(e -> e.getValue().Name));
		return entries;
	}

	public List<Map.Entry<String, CurseModpacks.CurseModpack>> filter(boolean reverse, String sorting, String version, String name) {
		List<Map.Entry<String, CurseModpacks.CurseModpack>> list = filter(pack -> pack.matchesVersion(version), pack -> pack.matchesName(name));
		if (sorting.equalsIgnoreCase("Popularity"))
			list.sort((o1, o2) -> reverse ? 1 : -1 * Double.compare(o1.getValue().getPopularityScore(), o2.getValue().getPopularityScore()));
		if (sorting.equalsIgnoreCase("Alphabetical"))
			list.sort((o1, o2) -> reverse ? 1 : -1 * o1.getValue().Name.compareTo(o2.getValue().Name));
		return list;
	}

	private List<Map.Entry<String, CurseModpacks.CurseModpack>> filter(Predicate<CurseModpack> version, Predicate<CurseModpack> name) {
		Predicate<CurseModpack> filter = pack -> (version == null || version.test(pack)) && (name == null || name.test(pack));
		Collection<Map.Entry<String, CurseModpacks.CurseModpack>> entries = Collections2.filter(getEntries(), e -> filter.test(e.getValue()));
		return entries.stream().filter(pack -> filter.test(pack.getValue())).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
