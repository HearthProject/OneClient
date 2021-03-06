package com.hearthproject.oneclient.api.modpack.curse.data;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.json.JsonUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CurseProjects extends HashMap<String, CurseProject> {

	@Override
	public CurseProject put(String key, CurseProject value) {
		value.Id = key;
		return super.put(key, value);
	}

	private List<Map.Entry<String, CurseProject>> getEntries() {
		List<Map.Entry<String, CurseProject>> entries = Lists.newArrayList(this.entrySet());
		entries.sort(Comparator.comparing(e -> e.getValue().Name));
		return entries;
	}

	public List<Map.Entry<String, CurseProject>> filter(boolean reverse, String sorting, String version, String search) {

		List<Map.Entry<String, CurseProject>> list = filter(pack -> pack.matchesVersion(version), search(search));
		if (sorting.equalsIgnoreCase("Popularity"))
			list.sort((o1, o2) -> (reverse ? 1 : -1) * Double.compare(o1.getValue().getPopularityScore(), o2.getValue().getPopularityScore()));
		if (sorting.equalsIgnoreCase("Alphabetical"))
			list.sort((o1, o2) -> (reverse ? -1 : 1) * o1.getValue().Name.compareTo(o2.getValue().Name));
		return list;
	}

	private List<Map.Entry<String, CurseProject>> filter(Predicate<CurseProject> version, Predicate<CurseProject> name) {
		Predicate<CurseProject> filter = pack -> (version == null || version.test(pack)) && (name == null || name.test(pack));
		Collection<Map.Entry<String, CurseProject>> entries = Collections2.filter(getEntries(), e -> filter.test(e.getValue()));
		return entries.stream().filter(pack -> filter.test(pack.getValue())).collect(Collectors.toList());
	}

	private static Pattern pattern = Pattern.compile("https://minecraft.curseforge.com/projects/(.*)");

	public Predicate<CurseProject> search(String search) {
		Matcher matcher = pattern.matcher(search);
		if (matcher.matches()) {
			search = matcher.group(1);
		}
		final String finalSearch = search;
		if (NumberUtils.isNumber(finalSearch)) {
			return pack -> pack.matchesId(finalSearch);
		}
		return pack -> pack.matchesName(finalSearch);
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
