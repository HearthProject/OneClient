package com.hearthproject.oneclient.api.curse;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.curse.data.CurseModpacks;
import com.hearthproject.oneclient.api.curse.data.CurseMods;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Curse {
	public static Cache<String, CurseModpacks> MODPACKS_CACHE;

	private static final String CURSE_META_BASE = "https://cursemeta.dries007.net/";
	private static final String CURSE_META_PROJECT = CURSE_META_BASE + "${projectID}.json";
	private static final String CURSE_META_FILES = CURSE_META_BASE + "${projectID}/files.json";
	private static final String CURSE_META_FILE = CURSE_META_BASE + "${projectID}/${fileID}.json";
	private static final String CURSE_META_MODPACKS = CURSE_META_BASE + "modpacks.json";
	private static final String CURSE_META_MODS = CURSE_META_BASE + "mods.json";

	public static void init() {
		RemovalListener<String, Object> removalListener = removal -> {
			if (removal.getCause() == RemovalCause.EXPIRED) {
				getModpacks();
			}
		};
		MODPACKS_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(removalListener).build();
		getModpacks();
	}

	public static URL getProjectURL(String projectID) {
		try {
			return new URL(CURSE_META_PROJECT.replace("${projectID}", projectID));
		} catch (MalformedURLException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static URL getProjectFilesURL(String projectID) {
		try {
			return new URL(CURSE_META_FILES.replace("${projectID}", projectID));
		} catch (MalformedURLException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static URL getFileURL(String projectID, String fileID) {
		try {
			return new URL(CURSE_META_FILE.replace("${projectID}", projectID).replace("${fileID}", fileID));
		} catch (MalformedURLException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static CurseMods getMods() {
		try {
			return JsonUtil.read(new URL(CURSE_META_MODS), CurseMods.class);
		} catch (MalformedURLException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static CurseModpacks getModpacks() {
		CurseModpacks packs = MODPACKS_CACHE.getIfPresent("SINGLETON");
		if (packs == null) {
			try {
				packs = JsonUtil.read(new URL(CURSE_META_MODPACKS), CurseModpacks.class);
				MODPACKS_CACHE.put("SINGLETON", packs);
			} catch (MalformedURLException e) {
				OneClientLogging.error(e);
			}
		}
		return packs;
	}

	public static List<CurseProject.CurseFile> getFiles(String projectId, String gameVersion) {
		CurseProject.CurseFile[] files = JsonUtil.read(Curse.getProjectFilesURL(projectId), CurseProject.CurseFile[].class);
		if (files != null) {
			List<CurseProject.CurseFile> curseFiles = Lists.newArrayList(files).stream().filter(file -> gameVersion.isEmpty() || file.getGameVersion().contains(gameVersion)).collect(Collectors.toList());
			curseFiles.forEach(f -> f.projectId = projectId);
			return curseFiles;
		}
		return null;
	}

}
