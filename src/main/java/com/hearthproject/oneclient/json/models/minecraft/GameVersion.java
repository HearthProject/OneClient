package com.hearthproject.oneclient.json.models.minecraft;

import java.util.List;

// https://launchermeta.mojang.com/mc/game/version_manifest.json
public class GameVersion {

	public Latest latest;
	public List<Version> versions;

	public class Latest {

		public String snapshot;
		public String release;

	}

	public class Version {

		public String id;
		public String type;
		public String time;
		public String releaseTime;
		public String url;

	}

}
