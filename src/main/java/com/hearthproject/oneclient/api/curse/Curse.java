package com.hearthproject.oneclient.api.curse;

import java.net.MalformedURLException;
import java.net.URL;

public class Curse {
	private static final String CURSE_META_BASE = "https://cursemeta.dries007.net/";
	private static final String CURSE_META_PROJECT = CURSE_META_BASE + "${projectID}.json";
	private static final String CURSE_META_FILES = CURSE_META_BASE + "${projectID}/files.json";
	private static final String CURSE_META_FILE = CURSE_META_BASE + "${projectID}/${fileID}.json";

	public static URL getProjectURL(String projectID) {
		try {
			return new URL(CURSE_META_PROJECT.replace("${projectID}", projectID));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL getProjectFilesURL(String projectID) {
		try {
			return new URL(CURSE_META_FILES.replace("${projectID}", projectID));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL getFileURL(int projectID, int fileID) {
		try {
			return new URL(CURSE_META_FILE.replace("${projectID}", "" + projectID).replace("${fileID}", "" + fileID));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
