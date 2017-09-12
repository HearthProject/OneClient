package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.IInstallable;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.json.JsonUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CurseInstaller extends ModInstaller {

	private CurseProject.CurseFile file;
	private Manifest manifest;

	public CurseInstaller(CurseProject.CurseFile file) {
		this.file = file;
	}

	@Override
	public void install(Instance instance) {
		File directory = FileUtil.findDirectory(Constants.TEMPDIR, instance.getName());
		File pack = FileUtil.extractFromURL(file.getDownloadURL(), directory);
		manifest = JsonUtil.read(new File(pack, "manifest.json"), Manifest.class);
		//TODO instance.setForgeVersion
		getMods().forEach(i -> i.install(instance));
		installOverrides(pack, directory);
		try {
			FileUtils.copyFile(new File(Constants.ICONDIR, instance.getName() + ".png"), new File(instance.getDirectory(), "icon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
