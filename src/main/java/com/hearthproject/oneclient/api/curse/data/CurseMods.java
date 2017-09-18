package com.hearthproject.oneclient.api.curse.data;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.json.JsonUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CurseMods extends HashMap<String, CurseModData> {
	@Override
	public CurseModData put(String key, CurseModData value) {
		value.Id = key;
		return super.put(key, value);
	}

	private List<Entry<String, CurseModData>> getEntries() {
		List<Map.Entry<String, CurseModData>> entries = Lists.newArrayList(this.entrySet());
		entries.sort(Comparator.comparing(e -> e.getValue().Name));
		return entries;
	}

	public List<Map.Entry<String, CurseModData>> filter(String version, String name) {
		return filter(mod -> mod.matchesVersion(version), pack -> pack.matchesName(name));
	}

	private List<Map.Entry<String, CurseModData>> filter(Predicate<CurseModData> version, Predicate<CurseModData> name) {
		Predicate<CurseModData> filter = mod -> (version == null || version.test(mod)) && (name == null || name.test(mod));
		Collection<Entry<String, CurseModData>> entries = Collections2.filter(getEntries(), e -> filter.test(e.getValue()));
		return entries.stream().filter(mod -> filter.test(mod.getValue())).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
