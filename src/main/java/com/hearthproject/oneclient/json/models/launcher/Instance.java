package com.hearthproject.oneclient.json.models.launcher;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Instance {

	private Manifest manifest;

	public Instance() {
		this.manifest = new Manifest();
	}

	public Instance(Manifest manifest) {
		this.manifest = manifest;
	}

	public void setManifest(Manifest manifest) {
		this.manifest = manifest;
	}

	public Manifest getManifest() {
		return manifest;
	}

	public File getDirectory() {
		if (manifest != null)
			return manifest.getDirectory();
		return null;
	}

	public void delete() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Are you sure you want to delete the pack");
		alert.setContentText("This will remove all mods and worlds, this cannot be undone!");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				ContentPanes.INSTANCES_PANE.button.fire();
				File dir = getDirectory();
				FileUtils.deleteDirectory(dir);
				InstanceManager.load();
				ContentPanes.INSTANCES_PANE.refresh();
			} catch (IOException e) {
				OneClientLogging.logger.error(e);
			}
		}
	}

	public void setName(String name) {
		int i = 0;
		manifest.name = name;
		while (!isValid()) {
			manifest.name = (name + "(" + i++ + ")");
		}
	}

	public boolean isValid() {
		return !getDirectory().exists();
	}

	public void save() {
		getManifest().save();
	}

	public void export() {
		try {
			File temp = FileUtil.findDirectory(Constants.TEMPDIR, "curseExport");

			File pack = FileUtil.findDirectory(temp, getManifest().getName());
			File overrides = FileUtil.findDirectory(pack, "overrides");

			File manifest = new File(getDirectory(), "manifest.json");

			List<File> files = Lists.newArrayList();
			files.add(manifest);
			//			Optional<List<File>> list = new FileSelection(getDirectory()).showAndWait();
			//			System.out.println(list.get());
			List<File> overrideFiles = selectOverrideFiles();
			for (File file : overrideFiles) {
				try {
					FileUtils.copyFile(file, overrides);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			files.add(overrides);

			ZipFile zip = new ZipFile(new File(getDirectory(), getManifest().getName() + ".zip"));
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			zip.addFile(manifest, parameters);
			pack.delete();
		} catch (ZipException e) {
			e.printStackTrace();
		}

	}

	private static final FileFilter MOD_FILES = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.or(FileFilterUtils.suffixFileFilter("zip"), FileFilterUtils.suffixFileFilter("jar"), FileFilterUtils.suffixFileFilter("disable")));

	public List<Mod> getMods() {
		return Arrays.stream(new File(getDirectory(), "mods").listFiles(MOD_FILES)).filter(File::isFile).map(Mod::new).sorted(Comparator.comparing(Mod::getName)).collect(Collectors.toList());
	}

	@Deprecated
	public static Manifest legacyLoadInstance(File directory) {
		Manifest manifest = null;
		File file = new File(directory, "instance.json");
		if (file.exists()) {
			JsonObject object = JsonUtil.read(file, JsonObject.class);
			String name = object.get("name").getAsString();
			String modloader = object.get("modLoader").getAsString().toLowerCase();
			String modloaderVersion = object.get("modLoaderVersion").getAsString();
			String icon = object.get("icon").getAsString();
			String minecraft = object.get("minecraftVersion").getAsString();

			manifest = new Manifest();
			manifest.setName(name);
			manifest.setModloader(modloader + "-" + modloaderVersion);
			manifest.setIcon(icon);
			manifest.setMinecraftVersion(minecraft);

			file.deleteOnExit();
		}
		return manifest;
	}

	public static Instance load(File directory) {
		Manifest manifest = legacyLoadInstance(directory);
		if (manifest == null)
			manifest = JsonUtil.read(new File(directory, "manifest.json"), Manifest.class);
		else {
			manifest.save();
		}
		if (manifest == null)
			return null;
		return new Instance(manifest);
	}

	public static List<File> selectOverrideFiles() {
		List<File> files = Lists.newArrayList();

		return files;
	}

}
