package com.hearthproject.oneclient.util.curse;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.FileData;

import java.io.File;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class CurseMetaUtils {
	//2014-12-20T00:13:27
	public static final SimpleDateFormat TIMEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final String CURSEMETA = "https://cursemeta.dries007.net/";

	public static List<ProjectFile> readProject(int projectID) {
		Type type = new TypeToken<ArrayList<ProjectFile>>() {}.getType();
		try {
			URL url = new URL(CURSEMETA + projectID + "/files.json");
			return JsonUtil.read(url, type);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Stream<ProjectFile> getFiles(int projectID, String version) {
		return readProject(projectID).stream()
			.filter(f -> Lists.newArrayList(f.GameVersion).contains(version))
			.sorted(Comparator.comparing(ProjectFile::date, Comparator.nullsLast(Comparator.reverseOrder())));
	}

	public static ProjectFile getLatest(int projectID, String version) {
		return getFiles(projectID, version).findFirst().orElse(null);
	}

	public static ProjectFile findProject(File file) {
		return null;
	}

	public static class ProjectFile {
		public String DownloadURL;
		public String FileDate;
		public String FileName;
		public String[] GameVersion;

		public int Id;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(DownloadURL).append("\n");
			builder.append(FileName).append("\n");
			builder.append(Arrays.toString(GameVersion)).append("\n");
			builder.append(Id).append("\n");
			return builder.toString();
		}

		public FileData getFileData(int projectID) {
			return new FileData(projectID, Id);
		}

		public Date date() {
			try {
				return TIMEFORMAT.parse(FileDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
