package com.hearthproject.oneclient.json.models.minecraft;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

		@Override
		public String toString() {
			return id;
		}
	}

	public Stream<Version> get(Predicate<Version> filter) {
		return versions.stream().filter(filter);
	}

}
