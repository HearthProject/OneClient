package com.hearthproject.oneclient.api.curse.data;

import com.hearthproject.oneclient.json.JsonUtil;

import java.util.HashMap;
import java.util.List;

public class CurseModpacks extends HashMap<String, CurseModpacks.CurseModpack> {

	public class CurseModpack {
		public List<LatestFile> GameVersionLatestFiles;
		public String Name;
		public String PrimaryAuthorName;
		public String Summary;
		public String WebSiteURL;
	}

	public class LatestFile {
		public String FileType;
		public String GameVesion;
		public String ProjectFileID;
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
