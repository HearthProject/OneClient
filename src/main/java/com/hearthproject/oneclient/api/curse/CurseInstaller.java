package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.IInstallable;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.api.curse.data.Manifest;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CurseInstaller extends ModInstaller {

	private List<CurseProject.CurseFile> files;
	private Manifest manifest;

	private CurseProject.CurseFile file = null;

	public CurseInstaller(List<CurseProject.CurseFile> file) {
		this.files = file;
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

		NotifyUtil.setText("Downloading {}", instance.getName());
		File directory = FileUtil.findDirectory(Constants.TEMPDIR, instance.getName());
		File pack = FileUtil.extractFromURL(file.getDownloadURL(), directory);
		manifest = JsonUtil.read(new File(pack, "manifest.json"), Manifest.class);
		instance.setName(manifest.name);
		NotifyUtil.setText("Installing {}", instance.getName());
		instance.setGameVersion(manifest.minecraft.version);
		instance.setForgeVersion(manifest.minecraft.getModloader());
		List<IInstallable> mods = getMods();
		int count = 0;
		for (IInstallable mod : mods) {
			NotifyUtil.setProgressText(count + "/" + mods.size());
			NotifyUtil.setProgress(((double) count) / mods.size());
			mod.install(instance);
			count++;
		}
		NotifyUtil.clear();
		installOverrides(pack, directory);
		//TODO get icon
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

	@Override
	public PackType getType() {
		return PackType.CURSE;
	}

	public List<IInstallable> getMods() {
		return manifest.files.stream().map(CurseMod::new).collect(Collectors.toList());
	}
}
