package com.hearthproject.oneclient.api.curse.data;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CurseFullProject {
	public String Id;
	private String Name;
	private String WebSiteURL;
	private String Summary;
	private List<CurseFile> LatestFiles;
	private List<Author> Authors;
	private List<Attachment> Attachments;
	private List<Category> Categories;
	private double DownloadCount;
	private double PopularityScore;

	public String getName() {
		return Name;
	}

	public String getAuthorsString() {
		return getAuthors().stream().map(CurseFullProject.Author::getName).collect(Collectors.joining(", "));
	}

	public String getVersions() {
		return getLatestFiles().stream().flatMap(file -> file.getGameVersion().stream()).distinct().collect(Collectors.joining(", "));
	}

	public List<CurseFile> getLatestFiles() {
		return LatestFiles;
	}

	public List<Author> getAuthors() {
		return Authors;
	}

	public URL getIcon() {
		if (Attachments != null) {
			String icon = Attachments.stream().filter(a -> a.IsDefault).map(a -> a.Url).findFirst().orElse(null);
			if (icon != null)
				try {
					return new URL(icon);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public List<Category> getCategories() {
		return Categories;
	}

	public String getWebSiteURL() {
		return WebSiteURL;
	}

	public double getPopularityScore() {
		return PopularityScore;
	}

	public List<CurseFile> getFiles(String gameVersion) {
		return Curse.getFiles(Id, gameVersion);
	}

	public double getDownloads() {
		return DownloadCount;
	}

	public String getSummary() {
		return Summary;
	}

	public Image getImage() {
		URL url = getIcon();
		if (url != null) {
			String name = null;
			try {
				name = URLEncoder.encode(getName(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			File file = new File(Constants.ICONDIR, name + ".png");
			FileUtil.downloadFromURL(url, file);
			return ImageUtil.openCachedImage(file);
		}
		return null;
	}

	public static class CurseFile implements Comparable<CurseFile> {
		private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		private String DownloadURL;
		private List<String> GameVersion;
		private String FileName;
		private String Id;
		private String FileDate;
		public String projectId;

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

		public Date getDate() {
			try {
				if (!FileDate.isEmpty())
					return DATE_FORMAT.parse(FileDate);
			} catch (ParseException e) {
				OneClientLogging.error("{} {}", projectId, e);
			}
			return new Date();
		}

		@Override
		public int compareTo(CurseFile o) {
			return -1 * getDate().compareTo(o.getDate());
		}

		public FileData toFileData() {
			return new FileData(projectId, Id);
		}
	}

	public class Author {
		private String Name, Url;

		public String getName() {
			return Name;
		}

		public String getUrl() {
			return Url;
		}
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