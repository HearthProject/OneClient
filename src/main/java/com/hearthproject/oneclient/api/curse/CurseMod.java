package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.api.IInstallable;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.FileData;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import org.apache.commons.io.FilenameUtils;

public class CurseMod implements IInstallable {
	private FileData fileData;

	public CurseMod(FileData fileData) {
		this.fileData = fileData;
	}

	@Override
	public void install(Instance instance) {
		NotifyUtil.setText("Installing %s", FilenameUtils.getBaseName(fileData.getURL()));
		FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
	}

	@Override
	public PackType getType() {
		return PackType.CURSE;
	}
}
