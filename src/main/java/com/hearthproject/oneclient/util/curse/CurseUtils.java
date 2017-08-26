package com.hearthproject.oneclient.util.curse;

import com.google.common.collect.Maps;
import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurseUtils {
	public static final Map<String, Image> IMAGE_CACHE = Maps.newHashMap();
	public static final String CURSE_BASE = "https://mods.curse.com";
	public static final String CURSEFORGE_BASE = "https://minecraft.curseforge.com";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:50.0) Gecko/20100101 Firefox/50.0";

	public static Filter versionFilter(String value) {
		return new Filter("filter-project-game-version=", value);
	}

	public static Filter page(String value) {
		return new Filter("page=", value);
	}

	public static List<String> getVersions() {
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft");
		Elements versions = d.select("#filter-project-game-version option");
		return versions.stream().map(Element::val).distinct().collect(Collectors.toList());
	}

	public static List<CursePack> getPacks(int page, String version) {
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/modpacks/minecraft", versionFilter(version), page(Integer.toString(page)));
		String realPage = d.select(".b-pagination-item").select(".s-active").first().text();
		if (Integer.parseInt(realPage) != page) {
			return null;
		}
		Elements packs = d.select("#addons-browse").first().select("ul > li > ul");
		return packs.stream().map(CursePack::new).collect(Collectors.toList());
	}

	public static List<CursePack> searchCurse(String query) {
		String formatQuery = query.replace(" ", "+");
		Document d = CurseUtils.getHtml(CurseUtils.CURSE_BASE, "/search", new Filter("?search=", formatQuery), new Filter("#t1:", "modpacks"));
		Elements results = d.select("#tab-modpacks .minecraft");
		return results.stream().map(CursePack.CurseSearch::new).collect(Collectors.toList());

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
			System.out.println(finalURL);
			return Jsoup.connect(finalURL).get();
		} catch (IOException e) {
			e.printStackTrace();
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

}
