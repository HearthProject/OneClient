package com.hearthproject.oneclient.api.curse.data;

import org.apache.commons.io.FilenameUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class CurseProject {
	private String Name;
	private List<CurseFile> LatestFiles;
	private List<Author> Authors;
	private List<Attachment> Attachments;

	public String getName() {
		return Name;
	}

	public List<CurseFile> getLatestFiles() {
		return LatestFiles;
	}

	public List<String> getAuthors() {
		return Authors.stream().map(a -> a.Name).collect(Collectors.toList());
	}

	public URL getIcon() {
		String icon = Attachments.stream().filter(a -> a.IsDefault).map(a -> a.Url).findFirst().orElse(null);
		if (icon != null)
			try {
				return new URL(icon);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		return null;
	}

	public static class CurseFile {
		private String DownloadURL;
		private List<String> GameVersion;
		private String FileName;
		private String Id;

		public String getId() {
			return Id;
		}

		public String getDownloadURL() {
			return DownloadURL;
		}

		public List<String> getGameVersion() {
			return GameVersion;
		}

		public String getFileName() {
			return FilenameUtils.removeExtension(FileName);
		}

		public boolean equals(String version) {
			return GameVersion.contains(version);
		}
	}

	private class Author {
		private String Name, Url;
	}

	private class Attachment {
		private String Description;
		private boolean IsDefault;
		private String ThumbnailUrl;
		private String Title;
		private String Url;
	}
}