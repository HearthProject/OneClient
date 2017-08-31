package com.hearthproject.oneclient.util.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class CurseUtils {

	public static final String CURSE_BASE = "https://mods.curse.com";
	public static final String CURSEFORGE_PROJECT_BASE = "https://www.curseforge.com/projects/";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:50.0) Gecko/20100101 Firefox/50.0";

	public static Filter versionFilter(String value) {
		return new Filter("filter-project-game-version=", value);
	}

	public static Filter sortingFilter(String value) {
		return new Filter("filter-project-sort=", value);
	}

	public static Filter page(String value) {
		return new Filter("page=", value);
	}

	public static List<String> getVersions() {
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft");
		Elements versions = d.select("#filter-project-game-version option");
		return versions.stream().map(Element::val).distinct().collect(Collectors.toList());
	}

	public static List<Pair<String, String>> getSorting() {
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft");
		Elements versions = d.select("#filter-project-sort option");
		return versions.stream().map(e -> new Pair<>(e.text(), e.val())).distinct().collect(Collectors.toList());
	}

	public static List<CurseElement> getMods(int page, String version, String sorting) {
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/mc-mods/minecraft", versionFilter(version), sortingFilter(sorting), page(Integer.toString(page)));
		Element e = d.select(".b-pagination-item").select(".s-active").first();
		String realPage = e != null ? e.text() : null;
		if (realPage == null) {
			return null;
		}
		if (Integer.parseInt(realPage) != page) {
			return Lists.newArrayList();
		}
		Elements packs = d.select("#addons-browse").first().select("ul > li > ul");
		return packs.stream().map(CurseElement::new).collect(Collectors.toList());
	}

	public static List<CurseElement> getPacks(int page, String version, String sorting) {
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft", versionFilter(version), sortingFilter(sorting), page(Integer.toString(page)));
		Element e = d.select(".b-pagination-item").select(".s-active").first();
		String realPage = e != null ? e.text() : null;
		if (realPage == null) {
			return null;
		}
		if (Integer.parseInt(realPage) != page) {
			return Lists.newArrayList();
		}
		Elements packs = d.select("#addons-browse").first().select("ul > li > ul");
		return packs.stream().map(CurseElement::new).collect(Collectors.toList());
	}

	public static List<CurseElement> searchCurse(String query, String type) {
		String formatQuery = query.replace(" ", "+");
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/search", new Filter("game-slug", "minecraft"), new Filter("search=", formatQuery), new Filter("#t1:", type));
		Elements results = d.select("#tab-modpacks .minecraft");
		return results.stream().map(CurseElement.CurseSearch::new).collect(Collectors.toList());

	}

	public static Document getHtml(String url, String path, Filter... filters) {
		try {
			String filter = "";
			if (filters != null && filters.length > 0) {
				StringBuilder builder = new StringBuilder();
				builder.append("?" + filters[0].toString());
				if (filters.length > 1) {
					for (int i = 1; i < filters.length; i++) {
						String f = filters[i].toString();
						if (!f.startsWith("#"))
							builder.append("&" + f);
						else
							builder.append(f);
					}
				}
				filter = builder.toString();
			}
			String finalURL = url + path + filter;
			return Jsoup.connect(finalURL).get();
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static class Filter {
		private String key, value;

		public Filter(String key, String value) {
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

}
