package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.Mod;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.api.curse.data.Manifest;
import com.hearthproject.oneclient.fx.nodes.PackUpdateDialog;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CurseInstaller extends ModInstaller {
	private transient CurseProject project;
	private transient List<CurseProject.CurseFile> files;
	private transient Manifest manifest;
	private CurseProject.CurseFile file;
	private String projectId;

	public CurseInstaller(CurseProject project) {
		super(PackType.CURSE);
		this.project = project;
		this.files = project.getFiles("");
		this.projectId = project.Id;
	}

	public void setFile(CurseProject.CurseFile file) {
		this.file = file;
	}

	public List<CurseProject.CurseFile> getFiles() {
		return files;
	}

	@Override
	public void install(Instance instance) {
		if (file == null) {
			OneClientLogging.error(new NullPointerException("No Curse File Selected"));
			return;
		}

		try {
			FileUtils.copyFile(new File(Constants.ICONDIR, instance.getName() + ".png"), new File(instance.getDirectory(), "icon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO more precise
		if (instance.getModDirectory().exists()) {
			try {
				FileUtils.deleteDirectory(instance.getModDirectory());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//TODO more precise
		if (instance.getConfigDirectory().exists()) {
			try {
				FileUtils.deleteDirectory(instance.getConfigDirectory());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		NotifyUtil.setText("Downloading %s", instance.getName());
		File directory = FileUtil.findDirectory(Constants.TEMPDIR, instance.getName());
		File pack = FileUtil.extractFromURL(file.getDownloadURL(), directory);
		NotifyUtil.setText("Extracting %s", instance.getName());
		manifest = JsonUtil.read(new File(pack, "manifest.json"), Manifest.class);
		NotifyUtil.setText("Installing %s", instance.getName());
		instance.setGameVersion(manifest.minecraft.version);
		instance.setForgeVersion(manifest.minecraft.getModloader());

		List<Mod> mods = getMods();
		AtomicInteger counter = new AtomicInteger(1);
		for (Mod mod : mods) {
			NotifyUtil.setProgressText(counter.get() + "/" + mods.size());
			NotifyUtil.setProgress(((double) counter.get()) / mods.size());
			mod.install(instance);
			counter.incrementAndGet();
		}

		NotifyUtil.setText("Copying Overrides");
		installOverrides(pack, instance.getDirectory());
		instance.setMods(FXCollections.observableArrayList(mods));
		NotifyUtil.clear();
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
					e.printStackTrace();
				}
			}
		}
	}

	public List<Mod> getMods() {
		return manifest.files.stream().map(CurseMod::new).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return JsonUtil.GSON.toJson(this);
	}

	public CurseProject.CurseFile findUpdate(Instance instance) {
		NotifyUtil.setText("%s Checking for updates", instance.getName());
		this.files = Curse.getFiles(projectId, "");
		if (this.files != null) {
			List<CurseProject.CurseFile> updates = Lists.newArrayList();
			for (CurseProject.CurseFile file : files) {
				if (file.compareTo(this.file) < 0) {
					updates.add(file);
				}
			}
			if (updates.isEmpty()) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("No Updates Available");
				alert.setHeaderText("No Updates Available");
				alert.setGraphic(null);
				alert.showAndWait();
				return null;
			}

			CurseProject.CurseFile file = new PackUpdateDialog(updates).showAndWait().orElse(null);
			this.file = file;
			return file;
		}
		return null;
	}

	@Override
	public void update(Instance instance) {
		CurseProject.CurseFile update = findUpdate(instance);
		if (update != null) {
			setFile(update);
			install(instance);
			instance.save();
			NotifyUtil.clear();
		}
	}
}
