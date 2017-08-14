package com.hearthproject.oneclient.json.models.forge;

import java.util.HashMap;

public class ForgeVersions {

	public String name;

	public HashMap<String, ForgeVersion> number;

	public HashMap<String, Integer> promos;

	public class ForgeVersion {
		public int build;
		public String mcversion;
		public String version;
	}

}
