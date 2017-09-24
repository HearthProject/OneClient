package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.util.files.FileHash;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class ModInstaller implements IInstallable {
	private PackType type;

	protected FileHash hash;
	protected String name;

	public ModInstaller(PackType type) {
		this.type = type;
	}

	public ModInstaller(PackType type, FileHash hash) {
		this.type = type;
		this.hash = hash;
		this.name = FilenameUtils.getBaseName(hash.getFilePath());
	}

	@Override
	public void install(Instance instance) {
		instance.mods.add(this);
	}

	public void update(Instance instance) { }

	@Override
	public PackType getType() {
		return type;
	}

	public boolean matches(File file) {
		return this.hash.matches(file);
	}

	public String getName() {
		return name;
	}

	public FileHash getHash() {
		return hash;
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

	@Override
	public String toString() {
		return getType().name();
	}
}
