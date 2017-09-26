package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.*;
import com.hearthproject.oneclient.api.curse.data.CurseFullProject;
import com.hearthproject.oneclient.api.curse.data.Manifest;
import com.hearthproject.oneclient.fx.nodes.UpdateDialog;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CurseInstaller extends ModpackInstaller {
	private transient CurseFullProject project;
	private transient List<CurseFullProject.CurseFile> files;
	private transient Manifest manifest;
	private CurseFullProject.CurseFile file;
	public String projectId;

	public CurseInstaller(CurseFullProject project) {
		super(PackType.CURSE);
		this.project = project;
		this.files = project.getFiles("");
		this.projectId = project.Id;
	}

	public void setFile(CurseFullProject.CurseFile file) {
		this.file = file;
	}

	public List<CurseFullProject.CurseFile> getFiles() {
		return files;
	}

	@Override
	public void install(Instance instance) {
		try {
			if (instance.checkCancel())
				return;
			if (file == null) {
				OneClientLogging.error(new NullPointerException("No Curse File Selected"));
				return;
			}
			if (instance.checkCancel())
				return;
			if (instance.checkCancel())
				return;
			//TODO more precise
			if (instance.getModDirectory().exists()) {
				try {
					FileUtils.deleteDirectory(instance.getModDirectory());
				} catch (IOException e) {
					OneClientLogging.error(e);
				}
			}
			if (instance.checkCancel())
				return;
			//TODO more precise
			if (instance.getConfigDirectory().exists()) {
				try {
					FileUtils.deleteDirectory(instance.getConfigDirectory());
				} catch (IOException e) {
					OneClientLogging.error(e);
				}
			}
			try {
				ImageUtil.downloadAndOpenImage((String) instance.tempInfo.get("icon-url"), instance.getName());
				FileUtils.copyFile(new File(Constants.ICONDIR, FileUtil.encode(instance.getName()) + ".png"), new File(instance.getDirectory(), "icon.png"));
			} catch (IOException e) {
				OneClientLogging.error(e);
			}

			if (instance.checkCancel())
				return;
			DownloadManager.updateMessage(instance.getName(), "Downloading %s", instance.getName());
			if (instance.checkCancel())
				return;
			File directory = FileUtil.findDirectory(Constants.TEMPDIR, instance.getName());
			File pack = FileUtil.extractFromURL(file.getDownloadURL(), directory);
			DownloadManager.updateMessage(instance.getName(), "Extracting %s", instance.getName());
			if (instance.checkCancel())
				return;
			manifest = JsonUtil.read(new File(pack, "manifest.json"), Manifest.class);
			DownloadManager.updateMessage(instance.getName(), "Installing %s", instance.getName());
			if (instance.checkCancel())
				return;

			instance.setGameVersion(manifest.minecraft.version);
			instance.setForgeVersion(manifest.minecraft.getModloader());
			DownloadManager.updateMessage(instance.getName(), "%s - version : %s forge : %s", instance.getName(), instance.getGameVersion(), instance.getForgeVersion());
			List<ModInstaller> mods = getMods();
			DownloadManager.updateMessage(instance.getName(), "%s - Installing %s Mods", instance.getName(), mods.size());
			AtomicInteger counter = new AtomicInteger(1);
			for (ModInstaller mod : mods) {
				DownloadManager.updateProgress(instance.getName(), counter.incrementAndGet(), mods.size());
				mod.setProcess(instance.getName());
				mod.install(instance);
				if (instance.checkCancel())
					return;
			}
			if (instance.checkCancel())
				return;
			DownloadManager.updateMessage(instance.getName(), "Copying Overrides");
			installOverrides(pack, instance.getDirectory());
			NotifyUtil.clear();
			if (instance.checkCancel())
				return;
		} catch (Throwable e) {
			OneClientLogging.error(e);
		}
		instance.verifyMods();
	}

	private void installOverrides(File pack, File instance) {
		File overrideDir = new File(pack, manifest.overrides);
		File[] files = overrideDir.listFiles();
		if (files != null) {
			for (File file : files) {
				File output = new File(instance, file.toString().replace(overrideDir.toString(), ""));
				try {
					if (file.isDirectory())
						FileUtils.copyDirectory(file, output);
					else if (file.isFile())
						FileUtils.copyFile(file, output);
				} catch (IOException e) {
					OneClientLogging.error(e);
				}
			}
		}
	}

	public List<ModInstaller> getMods() {
		OneClientLogging.info("{} - Collecting Mods", manifest.name);
		return manifest.files.stream().map(CurseModInstaller::new).collect(Collectors.toList());
	}


	@Override
	public String toString() {
		return String.format("%s:%s", getType(), project.Id);
	}

	public CurseFullProject.CurseFile findUpdate(Instance instance, boolean onlyNew) {
		NotifyUtil.setText("%s Checking for updates", instance.getName());
		this.files = Curse.getFiles(projectId, "");
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

	@Override
	public void update(Instance instance) {
		CurseFullProject.CurseFile update = findUpdate(instance, true);
		if (update != null) {
			setFile(update);
			install(instance);
			instance.save();
			NotifyUtil.clear();
		}
	}
}
