package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.api.curse.data.CurseModpacks;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;

import java.net.MalformedURLException;
import java.net.URL;

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
}
