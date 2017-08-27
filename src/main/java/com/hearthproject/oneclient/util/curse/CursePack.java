package com.hearthproject.oneclient.util.curse;

import com.google.common.collect.Lists;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CursePack {
	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	protected String title;
	private Future<String> getUrl, averageDownloads, totalDownloads, lastUpdated, created, version, icon;
	public Future<List<String>> authors;
	private Future<Document> getCursePage;

	private Document cursePage;

	public CursePack() {
	}

	public CursePack(Element element) {
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
			Element e = getCursePage.get().select(".screenshots img").first();
			return e.attr("src");
		});
		this.averageDownloads = executor.submit(() -> getCursePage.get().select(".average-downloads").text());
		this.totalDownloads = executor.submit(() -> getCursePage.get().select(".downloads").text());
		this.lastUpdated = executor.submit(() -> getCursePage.get().select(".updated").first().text());
		this.created = executor.submit(() -> getCursePage.get().select(".updated").last().text());
		this.version = executor.submit(() -> getCursePage.get().select(".version").text());
		this.authors = executor.submit(() -> getCursePage.get().select(".authors li").stream().map(Element::text).collect(Collectors.toList()));
	}

	public String getTitle() {
		return title;
	}

	public String getIcon() {
		try {
			return icon.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return "";
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

	public static class CurseSearch extends CursePack {
		public CurseSearch(Element element) {
			super();
			this.title = element.select("td > dl > dt > a").text();
			String url = CurseUtils.CURSE_BASE + element.select("td > dl > dt > a").attr("href");
			init(url);
		}
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
}

