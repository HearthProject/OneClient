package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.util.files.FileUtil;

import java.io.File;

public interface Instance {
	File getDirectory();

	String getName();

	default File getModDirectory() {
		return FileUtil.findDirectory(getDirectory(), "mods");
	}
}
