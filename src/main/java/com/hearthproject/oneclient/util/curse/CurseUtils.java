package com.hearthproject.oneclient.util.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.Manifest;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CurseUtils {
	private static final ExecutorService executor = Executors.newFixedThreadPool(1);

	public static final String CURSE_BASE = "https://mods.curse.com";
	public static final String CURSEFORGE_PROJECT_BASE = "https://www.curseforge.com/projects/";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:50.0) Gecko/20100101 Firefox/50.0";

	private static ObservableList<String> versions = FXCollections.emptyObservableList();
	private static ObservableList<Filter> sortings = FXCollections.emptyObservableList();

	public static Query versionFilter(String value) {
		return new Query("filter-project-game-version=", value);
	}

	public static Query sortingFilter(String value) {
		return new Query("filter-project-sort=", value);
	}

	public static Query page(String value) {
		return new Query("page=", value);
	}

	public static ObservableList<Filter> getSorting() {
		return sortings;
	}

	public static ObservableList<String> getVersions() {
		return versions;
	}

	public static void findVersions() {
		Optional<Document> document = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft");
		document.ifPresent(d -> versions = d.select("#filter-project-game-version option").stream().map(Element::val).distinct().collect(MiscUtil.toObservableList()));
	}

	public static void findSorting() {
		Optional<Document> document = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft");
		document.ifPresent(d -> sortings = d.select("#filter-project-sort option").stream().map(e -> new Filter(e.text(), e.val())).distinct().collect(MiscUtil.toObservableList()));
	}

	public static List<CurseElement> getMods(int page, String version, String sorting) {
		Optional<Document> d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/mc-mods/minecraft", versionFilter(version), sortingFilter(sorting), page(Integer.toString(page)));
		if (d.isPresent()) {
			Element e = d.get().select(".b-pagination-item").select(".s-active").first();
			String realPage = e != null ? e.text() : null;
			if (realPage == null) {
				return null;
			}
			if (Integer.parseInt(realPage) != page) {
				return Lists.newArrayList();
			}
			Elements packs = d.get().select("#addons-browse").first().select("ul > li > ul");
			return packs.stream().map(CurseElement::new).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}

	public static List<CurseElement> getPacks(int page, String version, String sorting) {
		Optional<Document> d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft", versionFilter(version), sortingFilter(sorting), page(Integer.toString(page)));
		if (d.isPresent()) {
			OneClientLogging.info("Loading page: {}", d.get().baseUri());
			Element e = d.get().select(".b-pagination-item").select(".s-active").first();
			String realPage = e != null ? e.text() : null;
			if (realPage == null) {
				return null;
			}
			if (Integer.parseInt(realPage) != page) {
				return Lists.newArrayList();
			}
			Elements packs = d.get().select("#addons-browse").first().select("ul > li > ul");
			return packs.stream().map(CurseElement::new).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}

	public static List<CurseElement> searchCurse(String query, String type) {
		String formatQuery = query.replace(" ", "+");
		Optional<Document> d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/search", new Query("game-slug", "minecraft"), new Query("search=", formatQuery), new Query("#t1:", type));
		if (d.isPresent()) {
			OneClientLogging.info("Searching Curse {}", d.get().baseUri());
			Elements results = d.get().select("#tab-" + type + " .minecraft");
			return results.stream().map(CurseElement.CurseSearch::new).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}

	public static Optional<Document> getHtml(String url, String path, Query... filters) {
		try {
			String filter = "";
			if (filters != null && filters.length > 0) {
				StringBuilder builder = new StringBuilder();
				builder.append("?").append(filters[0].toString());
				if (filters.length > 1) {
					for (int i = 1; i < filters.length; i++) {
						String f = filters[i].toString();
						if (!f.startsWith("#"))
							builder.append("&").append(f);
						else
							builder.append(f);
					}
				}
				filter = builder.toString();
			}
			String finalURL = url + path + filter;
			try {
				return Optional.ofNullable(Jsoup.connect(finalURL).get());
			}
			catch(UnknownHostException e) {
				OneClientLogging.error(e);
			}
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
		return Optional.empty();
	}

	public static class Query {
		private String key, value;

		public Query(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			if (value == null || value.isEmpty())
				return "";
			return String.format("%s%s", key, value);
		}

	}

	public static String getLocationHeader(String location) throws IOException, URISyntaxException {
		URI uri = new URI(location);
		HttpURLConnection connection = null;
		String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36";
		for (; ; ) {
			URL url = uri.toURL();
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", userAgent);
			connection.setInstanceFollowRedirects(false);
			String redirectLocation = connection.getHeaderField("Location");
			if (redirectLocation == null)
				break;

			// This gets parsed out later
			redirectLocation = redirectLocation.replaceAll("\\%20", " ");

			if (redirectLocation.startsWith("/"))
				uri = new URI(uri.getScheme(), uri.getHost(), redirectLocation, uri.getFragment());
			else {
				url = new URL(redirectLocation);
				uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			}
		}

		return uri.toString();
	}

	public static class Filter {
		private String display, value;

		public Filter(String display, String value) {
			this.display = display;
			this.value = value;
		}

		public String getDisplay() {
			return display;
		}

		public String getValue() {
			return value;
		}
	}

	public static class FilterConverter extends StringConverter<Filter> {
		private Map<String, Filter> map = new HashMap<>();

		@Override
		public String toString(Filter filter) {
			map.put(filter.getValue(), filter);
			return filter.getDisplay();
		}

		@Override
		public Filter fromString(String value) {
			return map.get(value);
		}
	}

	public static class VersionConverter extends StringConverter<String> {
		@Override
		public String toString(String s) {
			if (s.isEmpty())
				return "All";
			return s;
		}

		@Override
		public String fromString(String s) {
			if (s.equals("All"))
				return "";
			return s.replace(" ", "+");
		}
	}

	public static Manifest getManifest(File dir) throws IOException {
		NotifyUtil.setText("Parsing Manifest");
		File f = new File(dir, "manifest.json");
		if (!f.exists())
			throw new IllegalArgumentException("This modpack has no manifest");

		Manifest manifest = JsonUtil.GSON.fromJson(new FileReader(f), Manifest.class);

		return manifest;
	}

	public static String getZipURL(String url, String version) throws IOException, URISyntaxException {
		if (url.endsWith("/"))
			url = url.replaceAll(".$", "");

		String fileUrl;
		if (version.equals("latest"))
			fileUrl = url + "/files/latest";
		else
			fileUrl = url + "/files/" + version + "/download";
		return CurseUtils.getLocationHeader(fileUrl);
	}
}
