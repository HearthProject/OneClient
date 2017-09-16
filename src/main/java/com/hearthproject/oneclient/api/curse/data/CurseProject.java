package com.hearthproject.oneclient.api.curse.data;

import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.scene.control.Hyperlink;
import org.apache.commons.io.FilenameUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CurseProject {
	private String Name;
	private String WebSiteURL;
	private List<CurseFile> LatestFiles;
	private List<Author> Authors;
	private List<Attachment> Attachments;
	private List<Category> Categories;

	private double PopularityScore;

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

	public List<Category> getCategories() {
		return Categories;
	}

	public String getWebSiteURL() {
		return WebSiteURL;
	}

	public static class CurseFile implements Comparable<CurseFile> {
		private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		private String DownloadURL;
		private List<String> GameVersion;
		private String FileName;
		private String Id;
		private String FileDate;

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

		@Override
		public String toString() {
			return FileName;
		}

		private Date getDate() {
			try {
				return DATE_FORMAT.parse(FileDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return new Date();
		}

		@Override
		public int compareTo(CurseFile o) {
			return -1 * getDate().compareTo(o.getDate());
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

	public class Category {
		private String Id;
		private String Name;
		private String URL;

		//TODO category image views
		public Hyperlink getNode() {
			Hyperlink link = new Hyperlink(Name);
			link.setOnAction(event -> OperatingSystem.browseURI(URL));
			return link;
		}
	}
}