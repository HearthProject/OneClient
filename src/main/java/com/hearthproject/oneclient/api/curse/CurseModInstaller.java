package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.CurseFullProject;
import com.hearthproject.oneclient.api.curse.data.FileData;
import com.hearthproject.oneclient.util.files.FileHash;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class CurseModInstaller extends ModInstaller {

	private FileData fileData;
	private transient List<CurseFullProject.CurseFile> files;
	public transient CurseFullProject project;

	public CurseModInstaller(Instance instance, CurseFullProject data) {
		super(PackType.CURSE);
		this.project = data;
		this.name = project.getName();
		this.files = project.getFiles(instance.getGameVersion());
	}

	public CurseModInstaller(FileData fileData) {
		super(PackType.CURSE);
		this.fileData = fileData;
	}

	@Override
	public void install(Instance instance) {
		try {
			DownloadManager.updateMessage(instance.getName(), "%s - Installing %s", instance.getName(), FilenameUtils.getBaseName(fileData.getURL()));
			File mod = FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
			this.hash = new FileHash(mod);
			instance.getMods().add(this);
		} catch (Throwable e) {
			OneClientLogging.error(e);
		}

	}

	public FileData getFileData() {
		return fileData;
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}

	@Override
	public String toString() {
		return String.format("%s:%s", getType(), project.Id);
	}

	public List<CurseFullProject.CurseFile> getFiles() {
		return files;
	}
}


