package com.hearthproject.oneclient.util.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.image.Image;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CurseElement {
	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	protected String title;
	private Future<String> getUrl, averageDownloads, totalDownloads, lastUpdated, created, version, icon;
	public Future<List<String>> authors;
	private Future<Optional<Document>> getCursePage;

	private Optional<Document> cursePage;

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
		getUrl = executor.submit(() -> getCursePage.get().map(page -> "https:" + page.select(".curseforge a").attr("href")).orElse(""));

		this.icon = executor.submit(() -> getCursePage.get().map(page -> page.select(".primary-project-attachment").first().attr("src")).orElse(""));
		this.averageDownloads = executor.submit(() -> getCursePage.get().map(page -> page.select(".average-downloads").text()).orElse(""));
		this.totalDownloads = executor.submit(() -> getCursePage.get().map(page -> page.select(".downloads").text()).orElse(""));
		this.lastUpdated = executor.submit(() -> getCursePage.get().map(page -> page.select(".updated").first().text()).orElse(""));
		this.created = executor.submit(() -> getCursePage.get().map(page -> page.select(".updated").last().text()).orElse(""));
		this.version = executor.submit(() -> getCursePage.get().map(page -> page.select(".version").text()).orElse(""));
		this.authors = executor.submit(() -> getCursePage.get().map(page -> page.select(".authors li").stream().map(Element::text).collect(Collectors.toList())).orElse(Lists.newArrayList()));
	}

	public int getID() {
		String url = getUrl();
		if (url.isEmpty())
			return -1;
		return Integer.parseInt(url.replaceAll("\\D", ""));
	}

	public String getTitle() {
		return title;
	}

	public Optional<String> getIconURL() {
		try {
			return Optional.of(icon.get());
		} catch (InterruptedException | ExecutionException e) {
			OneClientLogging.error(e);
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
			return ImageUtil.openCachedImage(icon);
		}
		return null;
	}

	public String getAverageDownloads() {
		try {
			return this.averageDownloads.get();
		} catch (InterruptedException e) {
			OneClientLogging.error(e);
		} catch (ExecutionException e) {
			OneClientLogging.error(e);
		}
		return "";
	}

	public String getTotalDownloads() {
		try {
			return this.totalDownloads.get();
		} catch (InterruptedException e) {
			OneClientLogging.error(e);
		} catch (ExecutionException e) {
			OneClientLogging.error(e);
		}
		return "";
	}

	public String getCreatedDate() {
		try {
			return created.get();
		} catch (InterruptedException | ExecutionException e) {
			OneClientLogging.error(e);
		}
		return "";
	}

	public String getLastUpdated() {
		try {
			return lastUpdated.get();
		} catch (InterruptedException | ExecutionException e) {
			OneClientLogging.error(e);
		}
		return "";
	}

	public String getVersion() {
		try {
			return version.get();
		} catch (InterruptedException | ExecutionException e) {
			OneClientLogging.error(e);
		}
		return "";
	}

	public String getUrl() {
		try {
			return getUrl.get();
		} catch (InterruptedException | ExecutionException e) {
			OneClientLogging.error(e);
		}
		return "";
	}

	public List<String> getAuthors() {
		try {
			return authors.get();
		} catch (InterruptedException e) {
			OneClientLogging.error(e);
		} catch (ExecutionException e) {
			OneClientLogging.error(e);
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

