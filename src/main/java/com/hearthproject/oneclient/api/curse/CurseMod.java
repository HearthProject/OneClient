package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.Mod;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.FileData;
import com.hearthproject.oneclient.util.files.FileHash;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class CurseMod extends Mod {
	private FileData fileData;

	public CurseMod(FileData fileData) {
		super(PackType.CURSE);
		this.fileData = fileData;
	}

	@Override
	public void install(Instance instance) {
		NotifyUtil.setText("Installing %s", FilenameUtils.getBaseName(fileData.getURL()));
		File mod = FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
		this.file = new FileHash(mod);
	}

	public FileData getFileData() {
		return fileData;
	}
}
