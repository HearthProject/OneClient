package com.hearthproject.oneclient.json.models.modloader.forge;

import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.modloader.IModloader;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ForgeVersions {

	public String name;

	public HashMap<String, ForgeVersion> number;

	public HashMap<String, Integer> promos;

	public static class ForgeVersion implements IModloader {

		public int build;
		public String mcversion;
		public String version;
		public String branch;

		public ForgeVersion(String id) {
			this.version = id.substring(id.indexOf('-') + 1);
		}

		@Override
		public String toString() {
			return "forge-" + version;
		}
	}

	public List<ForgeVersion> get(Predicate<ForgeVersion> filter) {
		return number.entrySet().stream()
			.map(Map.Entry::getValue)
			.filter(filter)
			.sorted(Comparator.comparingInt(o -> -o.build))
			.collect(Collectors.toList());
	}

	public List<ForgeVersion> filterMCVersion(GameVersion.Version version) {
		return get(f -> f.mcversion.equalsIgnoreCase(version.id));
	}

}
