package com.hearthproject.oneclient.api.curse;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.CurseFullProject;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.api.curse.data.FileData;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.files.FileHash;
import com.hearthproject.oneclient.util.files.FileUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.concurrent.Executors;

public class CurseModInstaller extends ModInstaller {
	private final static ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
	public AsyncTask<CurseFullProject> fullProject;

	private FileData fileData;
	private CurseProject project;

	public CurseModInstaller(CurseProject data) {
		super(PackType.CURSE);
		this.name = data.Name;
		this.project = data;
		this.fullProject = new AsyncTask<>(() -> JsonUtil.read(Curse.getProjectURL(data.Id), CurseFullProject.class));
		service.execute(fullProject);
	}

	public CurseModInstaller(FileData fileData) {
		super(PackType.CURSE);
		this.fileData = fileData;
	}

	@Override
	public void install(Instance instance) {
		DownloadManager.updateMessage(getName(), "%s Installing %s", instance.getName(), FilenameUtils.getBaseName(fileData.getURL()));
		File mod = FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
		this.hash = new FileHash(mod);
	}

	public FileData getFileData() {
		return fileData;
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}

	public CurseProject getProject() {
		return project;
	}

}
