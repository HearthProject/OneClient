package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.CurseFullProject;
import com.hearthproject.oneclient.api.curse.data.FileData;
import com.hearthproject.oneclient.fx.nodes.UpdateDialog;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileHash;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Alert;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class CurseModInstaller extends ModInstaller {

	private FileData fileData;
	private transient List<CurseFullProject.CurseFile> files;
	private transient CurseFullProject.CurseFile file;
	public transient CurseFullProject project;
	public boolean resolveDependencies;

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
		if (fileData == null) {
			OneClientLogging.info("No File Selected");
		}
		try {
			if (resolveDependencies) {
				for (CurseFullProject.CurseFile.Dependency dep : file.getDependencies()) {
					if (dep.isRequired()) {
						CurseFullProject project = JsonUtil.read(Curse.getProjectURL(dep.AddOnId), CurseFullProject.class);
						CurseModInstaller depInstaller = new CurseModInstaller(instance, project);
						depInstaller.setFile(depInstaller.getFiles().stream().findFirst().orElse(null));
						DownloadManager.updateMessage(instance.getName(), "%s - Installing Dependency %s for %s", instance.getName(), FilenameUtils.getBaseName(depInstaller.fileData.getURL()), FilenameUtils.getBaseName(fileData.getURL()));
						depInstaller.install(instance);
					}
				}
			}
			DownloadManager.updateMessage(instance.getName(), "%s - Installing %s", instance.getName(), FilenameUtils.getBaseName(fileData.getURL()));
			File mod = FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
			this.hash = new FileHash(mod);
			instance.getMods().add(this);
		} catch (Throwable e) {
			OneClientLogging.error(e);
		}
	}

	@Override
	public void update(Instance instance) {
		CurseFullProject.CurseFile update = findUpdate(instance.getGameVersion(), true);
		if (update != null) {
			getHash().getFile().delete();
			setFile(update);
			install(instance);
			instance.verifyMods();
			NotifyUtil.clear();
		}
	}

	public CurseFullProject.CurseFile findUpdate(String gameVersion, boolean onlyNew) {
		NotifyUtil.setText("%s Checking for updates", getName());
		System.out.println(gameVersion);
		this.files = Curse.getFiles(project.Id, gameVersion);
		if (this.files != null) {
			List<CurseFullProject.CurseFile> updates = Lists.newArrayList();
			if (onlyNew) {
				for (CurseFullProject.CurseFile file : files) {
					if (file.compareTo(this.file) < 0) {
						updates.add(file);
					}
				}
			} else {
				updates.addAll(files);
			}
			if (updates.isEmpty()) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("No Updates Available");
				alert.setHeaderText("No Updates Available");
				alert.setGraphic(null);
				alert.showAndWait();
				return null;
			}

			CurseFullProject.CurseFile file = new UpdateDialog(updates).showAndWait().orElse(null);
			this.file = file;
			return file;
		}
		return null;
	}

	public FileData getFileData() {
		return fileData;
	}

	public void setFile(CurseFullProject.CurseFile file) {
		this.fileData = file.toFileData();
		this.file = file;
	}

	@Override
	public String toString() {
		return String.format("%s:%s", getType(), project.Id);
	}

	public List<CurseFullProject.CurseFile> getFiles() {
		return files;
	}

	public void setResolveDependencies(boolean resolveDependencies) {
		this.resolveDependencies = resolveDependencies;
	}
}


