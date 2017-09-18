package com.hearthproject.oneclient.api.curse.data;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.json.JsonUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CurseModpacks extends HashMap<String, CurseModpack> {

	@Override
	public CurseModpack put(String key, CurseModpack value) {
		value.Id = key;
		return super.put(key, value);
	}

	private List<Map.Entry<String, CurseModpack>> getEntries() {
		List<Map.Entry<String, CurseModpack>> entries = Lists.newArrayList(this.entrySet());
		entries.sort(Comparator.comparing(e -> e.getValue().Name));
		return entries;
	}

	public List<Map.Entry<String, CurseModpack>> filter(String version, String name) {
		return filter(pack -> pack.matchesVersion(version), pack -> pack.matchesName(name));
	}

	private List<Map.Entry<String, CurseModpack>> filter(Predicate<CurseModpack> version, Predicate<CurseModpack> name) {
		Predicate<CurseModpack> filter = pack -> (version == null || version.test(pack)) && (name == null || name.test(pack));
		Collection<Map.Entry<String, CurseModpack>> entries = Collections2.filter(getEntries(), e -> filter.test(e.getValue()));
		return entries.stream().filter(pack -> filter.test(pack.getValue())).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
