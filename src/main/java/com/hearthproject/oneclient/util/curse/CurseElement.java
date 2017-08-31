package com.hearthproject.oneclient.util.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import javafx.scene.image.Image;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CurseElement {
	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	protected String title;
	private Future<String> getUrl, averageDownloads, totalDownloads, lastUpdated, created, version, icon;
	public Future<List<String>> authors;
	private Future<Document> getCursePage;

	private Document cursePage;

	public CurseElement() {
	}

	public CurseElement(Element element) {
		this.title = element.select(".title a").text().trim().replaceAll("[^a-zA-Z0-9\\s]", "");

		String url = CurseUtils.CURSE_BASE + element.select(".title a").attr("href");
		init(url);
	}

	public void init(String curseUrl) {
		getCursePage = executor.submit(() -> {
			if (cursePage == null)
				cursePage = CurseUtils.getHtml(curseUrl, "");
			return cursePage;
		});
		getUrl = executor.submit(() -> {
			Elements link = getCursePage.get().select(".curseforge a");
			return "https:" + link.attr("href");
		});

		this.icon = executor.submit(() -> {
			Element e = getCursePage.get().select(".primary-project-attachment").first();
			return e.attr("src");
		});
		this.averageDownloads = executor.submit(() -> getCursePage.get().select(".average-downloads").text());
		this.totalDownloads = executor.submit(() -> getCursePage.get().select(".downloads").text());
		this.lastUpdated = executor.submit(() -> getCursePage.get().select(".updated").first().text());
		this.created = executor.submit(() -> getCursePage.get().select(".updated").last().text());
		this.version = executor.submit(() -> getCursePage.get().select(".version").text());
		this.authors = executor.submit(() -> getCursePage.get().select(".authors li").stream().map(Element::text).collect(Collectors.toList()));
	}

	public String getID() {
		String url = getUrl();
		if (url.isEmpty())
			return "";
		return url.replaceAll("\\D", "");
	}

	public String getTitle() {
		return title;
	}

	public Optional<String> getIconURL() {
		try {
			return Optional.of(icon.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public File getIcon() {
		Optional<String> url = getIconURL();
		if (url.isPresent()) {
			File dir = Constants.ICONDIR;
			File jpeg = new File(dir, getTitle() + ".jpeg");
			if (!jpeg.exists()) {
				FileUtil.downloadFromURL(url.get(), jpeg);
			}
			return jpeg;
		}
		return null;
	}

	public Image getIconImage() {
		File icon = getIcon();
		if (icon != null) {
			return ImageUtil.openImage(icon);
		}
		return null;
	}

	public String getAverageDownloads() {
		try {
			return this.averageDownloads.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getTotalDownloads() {
		try {
			return this.totalDownloads.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getCreatedDate() {
		try {
			return created.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getLastUpdated() {
		try {
			return lastUpdated.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getVersion() {
		try {
			return version.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getUrl() {
		try {
			return getUrl.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public List<String> getAuthors() {
		try {
			return authors.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return Lists.newArrayList();
	}

	public static class CurseSearch extends CurseElement {
		public CurseSearch(Element element) {
			super();
			this.title = element.select("td > dl > dt > a").text();
			String url = CurseUtils.CURSE_BASE + element.select("td > dl > dt > a").attr("href");
			init(url);
		}
	}
}

