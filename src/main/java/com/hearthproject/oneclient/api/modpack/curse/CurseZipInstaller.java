package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.api.modpack.*;
import com.hearthproject.oneclient.api.modpack.curse.data.Manifest;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.hearthproject.oneclient.util.MiscUtil.checkCancel;

public class CurseZipInstaller extends ModpackInstaller {
	private transient Manifest manifest;
	private transient File pack;

	public CurseZipInstaller(Manifest manifest, File pack) {
		super(PackType.CURSE);
		this.manifest = manifest;
		this.pack = pack;
	}

	@Override
	public void install(Instance instance) {
		try {
			instance.setGameVersion(manifest.minecraft.version);
			instance.setForgeVersion(manifest.minecraft.getModloader());
			DownloadManager.updateMessage(instance.getName(), "Installing %s", instance.getName());
			if (checkCancel())
				return;
			DownloadManager.updateMessage(instance.getName(), "%s - version : %s forge : %s", instance.getName(), instance.getGameVersion(), instance.getForgeVersion());
			List<ModInstaller> mods = getMods();
			AtomicInteger counter = new AtomicInteger(1);
			for (ModInstaller mod : mods) {
				DownloadManager.updateProgress(instance.getName(), counter.incrementAndGet(), mods.size());
				mod.install(instance);
				if (checkCancel())
					return;
			}
			if (checkCancel())
				return;
			DownloadManager.updateMessage(instance.getName(), "Copying Overrides");
			installOverrides(pack, instance.getDirectory());
			if (checkCancel())
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
}
