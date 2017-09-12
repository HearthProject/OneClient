package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.api.IInstallable;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.util.files.FileUtil;

public class CurseMod implements IInstallable {
	private FileData fileData;

	public CurseMod(FileData fileData) {
		this.fileData = fileData;
	}

	@Override
	public void install(Instance instance) {
		FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
	}

	@Override
	public PackType getType() {
		return PackType.CURSE;
	}
}
