package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.curse.data.CurseModpacks;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class Curse {
	private static final String CURSE_META_BASE = "https://cursemeta.dries007.net/";
	private static final String CURSE_META_PROJECT = CURSE_META_BASE + "${projectID}.json";
	private static final String CURSE_META_FILES = CURSE_META_BASE + "${projectID}/files.json";
	private static final String CURSE_META_FILE = CURSE_META_BASE + "${projectID}/${fileID}.json";
	private static final String CURSE_META_MODPACKS = CURSE_META_BASE + "modpacks.json";

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

	public static URL getFileURL(int projectID, int fileID) {
		try {
			return new URL(CURSE_META_FILE.replace("${projectID}", "" + projectID).replace("${fileID}", "" + fileID));
		} catch (MalformedURLException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static CurseModpacks getModpacks() {
		try {
			return JsonUtil.read(new URL(CURSE_META_MODPACKS), CurseModpacks.class);
		} catch (MalformedURLException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static List<CurseProject.CurseFile> getFiles(String projectId, String gameVersion) {
		CurseProject.CurseFile[] files = JsonUtil.read(Curse.getProjectFilesURL(projectId), CurseProject.CurseFile[].class);
		if (files != null) {
			return Lists.newArrayList(files).stream().sorted().filter(file -> gameVersion.isEmpty() || file.getGameVersion().contains(gameVersion)).collect(Collectors.toList());
		}
		return null;
	}

}
