package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.modpack.IExporter;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.ModInstaller;
import com.hearthproject.oneclient.api.modpack.PackType;
import com.hearthproject.oneclient.api.modpack.curse.data.Manifest;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
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
		instance.verifyMods();
		new Thread(() -> {
			System.out.println(instance.getMods());
			CurseInstaller installer = null;
			if (instance.installer instanceof CurseInstaller) {
				installer = (CurseInstaller) instance.installer;
			}

			Manifest manifest = new Manifest();
			manifest.manifestType = "minecraftModpack";
			manifest.manifestVersion = 1;
			try {
				if (MinecraftAuthController.getAuthentication() != null)
					manifest.author = MinecraftAuthController.getUsername(MinecraftAuthController.getAuthentication());
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
			manifest.name = instance.getName();
			manifest.version = instance.getPackVersion();
			manifest.author = "";
			if (installer != null) {
				manifest.projectID = installer.projectId;
			}
			manifest.files = instance.getMods().stream().filter(m -> m.getType() == PackType.CURSE).map(m -> ((CurseModInstaller) m).getFileData()).collect(Collectors.toList());
			manifest.overrides = "overrides";

			Manifest.Minecraft.Modloader forge = new Manifest.Minecraft.Modloader(instance.getForgeVersion());
			Manifest.Minecraft minecraft = new Manifest.Minecraft();

			minecraft.version = instance.getGameVersion();
			minecraft.modLoaders.add(forge);
			manifest.minecraft = minecraft;

			File temp = FileUtil.findDirectory(Constants.TEMPDIR, instance.getName());
			File overrides = FileUtil.findDirectory(temp, "overrides");
			File mods = FileUtil.findDirectory(overrides, "mods");
			File config = FileUtil.findDirectory(overrides, "config");

			File manifestJson = new File(temp, "manifest.json");
			//manifest
			JsonUtil.save(manifestJson, JsonUtil.GSON.toJson(manifest, Manifest.class));
			//configs
			try {
				FileUtils.copyDirectory(instance.getConfigDirectory(), config);
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
			//override mods
			List<ModInstaller> manual = instance.getMods().stream().filter(m -> m.getType().equals(PackType.MANUAL)).collect(Collectors.toList());
			for (ModInstaller mod : manual) {
				File file = mod.getHash().getFile();

				try {
					FileUtils.copy(new FileInputStream(file), new File(mods, file.getName()));
				} catch (IOException e) {
					OneClientLogging.error(e);
				}
			}
			try {
				ZipFile zip = new ZipFile(new File(Constants.EXPORTS, instance.getName() + ".zip"));
				ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
				parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
				zip.addFile(manifestJson, parameters);
				zip.addFolder(overrides, parameters);
			} catch (ZipException e) {
				OneClientLogging.error(e);
			}

		}).start();

	}

}
