package com.hearthproject.oneclient.util.curse;

import com.google.gson.Gson;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.tracking.OneClientTracking;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hearthproject.oneclient.util.curse.CurseUtils.getLocationHeader;

//Taken from: https://github.com/Vazkii/CMPDL/tree/master/src/vazkii/cmpdl + changed a bit
public class CursePackInstaller {
	public final Gson GSON_INSTANCE = new Gson();

	public final Pattern FILE_NAME_URL_PATTERN = Pattern.compile(".*?/([^/]*)$");

	public List<String> missingMods = null;

	public void downloadFromURL(String url, String version, Instance instance) throws Exception {
		missingMods = new ArrayList<>();
		instance.curseVersion = version;
		instance.curseURL = url;

		Matcher matcher = FILE_NAME_URL_PATTERN.matcher(instance.getZipURL());
		if (matcher.matches()) {
			String filename = matcher.group(1);
			File zipFile = downloadPackZip(filename, new URL(instance.getZipURL()));
			installPack(instance, zipFile);
		}
	}

	public void importFromZip(Instance instance, File zipFile) throws Exception {
		File newDir = new File(getTempPackDir(zipFile), zipFile.getName());
		Files.copy(zipFile.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		installPack(instance, newDir);
	}

	public void installPack(Instance instance, File zipFile) throws Exception {
		InstanceManager.setInstanceInstalling(instance, true);
		NotifyUtil.setText("Modpack filename is %s", zipFile.getName());
		File unzippedDir = unzipPack(getTempPackDir(zipFile), zipFile);
		Manifest manifest = getManifest(unzippedDir);
		instance.minecraftVersion = manifest.minecraft.version;
		instance.modLoader = "forge";
		instance.modLoaderVersion = manifest.getForgeVersion();
		instance.name = manifest.name;
		instance.curseVersion = manifest.version;
		int i = 1;
		while (!InstanceManager.isValid(instance)) {
			instance.name = manifest.name + "(" + i++ + ")";
		}
		InstanceManager.addInstance(instance);
		InstanceManager.setInstanceInstalling(instance, false);
		ContentPanes.INSTANCES_PANE.refresh();

		NotifyUtil.setText("Downloading %s", manifest.name);
		OneClientTracking.sendRequest("curse/install/" + manifest.name + "/" + manifest.version);
		File minecraftDir = instance.getDirectory();

		downloadModpackFromManifest(minecraftDir, manifest);
		copyOverrides(manifest, unzippedDir, minecraftDir);

		NotifyUtil.setText("Done downloading element %s", manifest.name);
		NotifyUtil.clear();
		missingMods = null;
	}

	public File downloadPackZip(String pack, URL url) throws IOException {
		File packDir = getTempPackDir(pack);
		String zipName = pack;
		if (!zipName.endsWith(".zip"))
			zipName = zipName + ".zip";
		NotifyUtil.setText("Downloading : %s", zipName);
		File zipFile = new File(packDir, zipName);
		downloadFileFromURL(zipFile, url);
		return zipFile;
	}

	public File unzipPack(File dir, File packZip) throws IOException, ZipException {
		NotifyUtil.setText("Unzipping Modpack Download");
		ZipFile zip = new ZipFile(packZip);
		zip.extractAll(dir.toString());
		return dir;
	}

	public File getTempDir() {
		return new File(Constants.TEMPDIR, "curseDownload");
	}

	public File getTempPackDir(String pack) {
		return FileUtil.findDirectory(getTempDir(), FilenameUtils.removeExtension(pack));
	}

	public File getTempPackDir(File zipFile) {
		return getTempPackDir(zipFile.getName());
	}

	public Manifest getManifest(File dir) throws IOException {
		NotifyUtil.setText("Parsing Manifest");
		File f = new File(dir, "manifest.json");
		if (!f.exists())
			throw new IllegalArgumentException("This modpack has no manifest");

		Manifest manifest = GSON_INSTANCE.fromJson(new FileReader(f), Manifest.class);

		return manifest;
	}

	public int left;

	public File downloadModpackFromManifest(File outputDir, Manifest manifest) throws IOException, URISyntaxException {
		int total = manifest.files.size();
		NotifyUtil.setText("Downloading modpack from manifest");
		NotifyUtil.setText("Manifest contains %s files to download", total);

		File modsDir = FileUtil.findDirectory(outputDir, "mods");

		left = total;

		manifest.files.stream().forEach(f -> {
			left--;
			try {
				downloadFile(f, modsDir, left, total);
			} catch (IOException | URISyntaxException e) {
				OneClientLogging.error(e);
			}
		});

		NotifyUtil.setText("Mod downloads complete.");
		return outputDir;
	}

	public void copyOverrides(Manifest manifest, File tempDir, File outDir) throws IOException {
		NotifyUtil.setText("Copying Modpack overrides");
		File overridesDir = new File(tempDir, manifest.overrides);

		Files.walk(overridesDir.toPath()).forEach(path -> {
			try {
				NotifyUtil.setText("Override: %s", path.getFileName());
				Files.copy(path, Paths.get(path.toString().replace(overridesDir.toString(), outDir.toString())), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
		});
	}

	public void downloadFile(Manifest.FileData file, File modsDir, int remaining, int total) throws IOException, URISyntaxException {
		NotifyUtil.setProgressAscend(remaining, total);
		String baseUrl = "http://minecraft.curseforge.com/projects/" + file.projectID;
		String projectUrl = getLocationHeader(baseUrl);
		projectUrl = projectUrl.replaceAll("\\?cookieTest=1", "");
		String fileDlUrl = projectUrl + "/files/" + file.fileID + "/download";
		String finalUrl = getLocationHeader(fileDlUrl);

		Matcher m = FILE_NAME_URL_PATTERN.matcher(finalUrl);
		if (!m.matches())
			throw new IllegalArgumentException("Mod file doesn't match filename pattern");

		String filename = m.group(1);
		filename = URLDecoder.decode(filename, "UTF-8");
		NotifyUtil.setText("Downloading %s", filename);
		if (filename.endsWith("cookieTest=1")) {
			NotifyUtil.setText("Missing file:%s. Skipped", filename);
			missingMods.add(finalUrl);
		} else {
			File f = new File(modsDir, filename);
			try {
				if (filename.equals("download"))
					throw new FileNotFoundException("Invalid filename");

				if (f.exists())
					NotifyUtil.setText("%s already downloaded. Skipped", filename);
				else
					downloadFileFromURL(f, new URL(finalUrl));
			} catch (FileNotFoundException e) {
				OneClientLogging.error(e);
				missingMods.add(finalUrl);
			}
		}
	}

	public void downloadFileFromURL(File f, URL url) throws IOException {
		FileUtils.copyURLToFile(url, FileUtil.createFile(f));
	}

}
