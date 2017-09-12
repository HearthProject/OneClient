package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.json.JsonUtil;

import java.io.File;

public interface Instance {
	File getDirectory();

	String getName();

	default File getModDirectory() {
		return FileUtil.findDirectory(getDirectory(), "mods");
	}

	void install();

	void delete();

	void update();

	default void save() {
		JsonUtil.save(new File(getDirectory(), "instance.json"), toString());
	}
}
