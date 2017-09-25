package com.hearthproject.oneclient.hearth.api.json.packs;

import com.hearthproject.oneclient.hearth.api.HearthApi;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ModPack {

	public String name;
	public String iconURL;
	public String description;
	public String recomendedVersion;
	public String owner;
	public List<ModPackVersion> versions;

	public static class ModPackVersion {
		public String version;
		public String packType;
		public String changelog;
		public String downloadURL;

		public ModPackVersion(String version, String packType, String changelog, String downloadURL) {
			this.version = version;
			this.packType = packType;
			this.changelog = changelog;
			this.downloadURL = downloadURL;
		}

		public ModPackVersion() {
		}
	}

	public void save(File file) throws IOException {
		FileUtils.writeStringToFile(file, HearthApi.GSON.toJson(this), StandardCharsets.UTF_8);
	}

}
