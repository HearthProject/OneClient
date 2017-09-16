package com.hearthproject.oneclient.api.curse.data;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.json.JsonUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	public List<Map.Entry<String, CurseModpacks.CurseModpack>> filter(String version, String name) {
		return filter(pack -> pack.matchesVersion(version), pack -> pack.matchesName(name));
	}

	private List<Map.Entry<String, CurseModpacks.CurseModpack>> filter(Predicate<CurseModpack> version, Predicate<CurseModpack> name) {

		Predicate<CurseModpack> filter = pack -> (version == null || version.test(pack)) && (name == null || name.test(pack));

		return getEntries().stream().filter(pack -> filter.test(pack.getValue())).collect(Collectors.toList());

	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
