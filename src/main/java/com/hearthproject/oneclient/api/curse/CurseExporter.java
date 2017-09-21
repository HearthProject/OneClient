package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.IExporter;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.Mod;
import com.hearthproject.oneclient.api.PackType;
import com.hearthproject.oneclient.api.curse.data.Manifest;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CurseExporter implements IExporter {

	@Override
	public void export(Instance instance) {
		CurseInstaller installer = (CurseInstaller) instance.installer;
		Manifest manifest = new Manifest();
		manifest.name = instance.getName();
		manifest.version = instance.getPackVersion();
		manifest.author = "";
		manifest.projectID = installer.projectId;
		manifest.files = instance.getMods().stream().filter(m -> m instanceof CurseModInstall).map(m -> ((CurseModInstall) m).getFileData()).collect(Collectors.toList());
		manifest.overrides = "overrides";

		Manifest.Minecraft.Modloader forge = new Manifest.Minecraft.Modloader(instance.getForgeVersion());
		Manifest.Minecraft minecraft = new Manifest.Minecraft();

		minecraft.version = instance.getGameVersion();
		minecraft.modLoaders.add(forge);

		File dir = FileUtil.findDirectory(Constants.EXPORTS, instance.getName());
		File overrides = FileUtil.findDirectory(dir, "overrides");
		File mods = FileUtil.findDirectory(overrides, "mods");
		File config = FileUtil.findDirectory(overrides, "config");

		File manifestJson = new File(dir, "manifest.json");
		//manifest
		JsonUtil.save(manifestJson, JsonUtil.GSON.toJson(manifest, Manifest.class));
		//configs
		try {
			FileUtils.copyDirectory(instance.getConfigDirectory(), config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//override mods
		List<Mod> manual = instance.getMods().stream().filter(m -> m.getType().equals(PackType.MANUAL)).collect(Collectors.toList());
		for (Mod mod : manual) {
			File file = mod.getHash().getFile();

			try {
				FileUtils.copy(new FileInputStream(file), new File(mods, file.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ZipFile zip = new ZipFile(new File(dir, instance.getName() + ".zip"));
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			zip.addFile(manifestJson, parameters);
			zip.addFolder(overrides, parameters);
		} catch (ZipException e) {
			e.printStackTrace();
		}

	}
}
