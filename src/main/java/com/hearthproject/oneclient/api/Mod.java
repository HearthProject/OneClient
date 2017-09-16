package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileHash;

import java.io.File;

public class Mod implements IInstallable {
	private PackType type;

	protected FileHash file;

	public Mod(PackType type) {
		this.type = type;
	}

	public Mod(PackType type, FileHash file) {
		this.type = type;
		this.file = file;
	}

	@Override
	public void install(Instance instance) {}

	@Override
	public PackType getType() {
		return type;
	}

	public boolean matches(File file) {
		return this.file.matches(file);
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}
}
