package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.Mod;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.CurseModData;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.api.curse.data.FileData;
import com.hearthproject.oneclient.util.files.FileHash;
import com.hearthproject.oneclient.util.files.FileUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class CurseMod extends Mod {

	private transient List<CurseProject.CurseFile> files;
	public transient CurseModData data;
	private FileData fileData;

	public CurseMod(CurseModData data) {
		super(PackType.CURSE);
		this.data = data;
		this.files = Curse.getFiles(data.Id, "");
	}

	public CurseMod(FileData fileData) {
		super(PackType.CURSE);
		this.fileData = fileData;
	}

	@Override
	public void install(Instance instance) {
		DownloadManager.updateMessage(instance.getName(), "Installing %s", FilenameUtils.getBaseName(fileData.getURL()));
		File mod = FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
		this.hash = new FileHash(mod);
		super.install(instance);
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}

	public FileData getFileData() {
		return fileData;
	}

	public List<CurseProject.CurseFile> getFiles() {
		return files;
	}

	@Override
	public String getName() {
		if (data != null)
			return data.getName();
		return super.getName();
	}
}
