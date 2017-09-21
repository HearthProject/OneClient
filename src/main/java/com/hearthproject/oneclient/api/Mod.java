package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileHash;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class Mod implements IInstallable {
	private PackType type;

	protected FileHash hash;

	public Mod(PackType type) {
		this.type = type;
	}

	public Mod(PackType type, FileHash hash) {
		this.type = type;
		this.hash = hash;
	}

	@Override
	public void install(Instance instance) {
		instance.mods.add(this);
	}

	@Override
	public PackType getType() {
		return type;
	}

	public boolean matches(File file) {
		return this.hash.matches(file);
	}

	public String getName() {
		return FilenameUtils.getBaseName(hash.getFilePath());
	}

	public FileHash getHash() {
		return hash;
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}

	public void toggleEnabled() {
		if (isEnabled()) {
			getHash().getFile().renameTo(new File(getHash().getFilePath() + ".disabled"));
		} else {
			getHash().getFile().renameTo(new File(getHash().getFilePath().replace(".disabled", "")));
		}
	}

	public boolean isEnabled() {
		if (getHash() != null)
			return !FilenameUtils.isExtension(getHash().getFilePath(), "disabled");
		return false;
	}

}
