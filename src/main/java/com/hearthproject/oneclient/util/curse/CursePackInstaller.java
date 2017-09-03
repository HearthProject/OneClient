package com.hearthproject.oneclient.util.curse;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.launcher.Manifest;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hearthproject.oneclient.util.curse.CurseUtils.getLocationHeader;

//Taken from: https://github.com/Vazkii/CMPDL/tree/master/src/vazkii/cmpdl + changed a bit
public class CursePackInstaller {
	private static boolean busy = false;

	public static final Pattern FILE_NAME_URL_PATTERN = Pattern.compile(".*?/([^/]*)$");

	public static Instance downloadFromURL(String url, String version) throws Exception {
		Instance instance = null;
		if (busy)
			return instance;
		busy = true;
		String zipURL = CurseUtils.getZipURL(url, version);
		Matcher matcher = FILE_NAME_URL_PATTERN.matcher(zipURL);
		if (matcher.matches()) {
			String filename = matcher.group(1);
			File zipFile = downloadPackZip(filename, new URL(zipURL));
			instance = installPack(zipFile);
		}
		busy = false;
		return instance;
	}

	public static Instance importFromZip(File zipFile) throws Exception {
		Instance instance = null;
		if (busy)
			return instance;
		busy = true;
		File newDir = new File(getTempPackDir(zipFile), zipFile.getName());
		Files.copy(zipFile.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		instance = installPack(newDir);
		busy = false;
		return instance;
	}

	public static Instance installPack(File zipFile) throws Exception {

		NotifyUtil.setText("Modpack filename is %s", zipFile.getName());
		File unzippedDir = unzipPack(getTempPackDir(zipFile), zipFile);
		Manifest manifest = CurseUtils.getManifest(unzippedDir);
		Instance instance = new Instance(manifest);
		InstanceManager.setInstanceInstalling(instance, true);
		instance.setManifest(manifest);
		instance.setName(manifest.name);

		InstanceManager.setInstanceInstalling(instance, false);
		ContentPanes.INSTANCES_PANE.refresh();

		NotifyUtil.setText("Downloading %s", manifest.name);
		OneClientTracking.sendRequest("curse/install/" + manifest.name + "/" + manifest.version);
		File minecraftDir = instance.getDirectory();

		downloadModpackFromManifest(minecraftDir, manifest);
		copyOverrides(manifest, unzippedDir, minecraftDir);

		InstanceManager.addInstance(instance);
		NotifyUtil.setText("Done downloading element %s", manifest.name);
		NotifyUtil.clear();

		return instance;
	}

	public static File downloadPackZip(String pack, URL url) throws IOException {
		File packDir = getTempPackDir(pack);
		String zipName = pack;
		if (!zipName.endsWith(".zip"))
			zipName = zipName + ".zip";
		NotifyUtil.setText("Downloading : %s", zipName);
		File zipFile = new File(packDir, zipName);
		downloadFileFromURL(zipFile, url);
		return zipFile;
	}

	public static File unzipPack(File dir, File packZip) throws IOException, ZipException {
		NotifyUtil.setText("Unzipping Modpack Download");
		ZipFile zip = new ZipFile(packZip);
		zip.extractAll(dir.toString());
		return dir;
	}

	public static File getTempDir() {
		return new File(Constants.TEMPDIR, "curseDownload");
	}

	public static File getTempPackDir(String pack) {
		return FileUtil.findDirectory(getTempDir(), FilenameUtils.removeExtension(pack));
	}

	public static File getTempPackDir(File zipFile) {
		return getTempPackDir(zipFile.getName());
	}

	private static int left;

	public static File downloadModpackFromManifest(File outputDir, Manifest manifest) throws IOException, URISyntaxException {
		int total = manifest.files.size();
		NotifyUtil.setText("Downloading modpack from manifest");
		NotifyUtil.setText("Manifest contains %s files to download", total);

		File modsDir = FileUtil.findDirectory(outputDir, "mods");

		left = total;

		manifest.files.parallelStream().forEach(f -> {
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

	public static void copyOverrides(Manifest manifest, File tempDir, File outDir) throws IOException {
		NotifyUtil.setText("Copying Modpack overrides");
		File overridesDir = new File(tempDir, manifest.overrides);

		for (File file : overridesDir.listFiles()) {
			File output = new File(outDir, file.toString().replace(overridesDir.toString(), ""));
			OneClientLogging.info("{} {}", file, output);
			NotifyUtil.setText("Override: %s", file);
			if (file.isDirectory())
				FileUtils.copyDirectory(file, output);
			else if (file.isFile())
				FileUtils.copyFile(file, output);
		}
	}

	public static void downloadFile(Manifest.FileData file, File modsDir, int remaining, int total) throws IOException, URISyntaxException {
		NotifyUtil.setProgressDescend(remaining, total);
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
		NotifyUtil.setText("Downloading: %s", filename);
		if (filename.endsWith("cookieTest=1")) {
			NotifyUtil.setText("Skipped: %s is missing. ", filename);
		} else {
			File f = new File(modsDir, filename);
			try {
				if (filename.equals("download"))
					throw new FileNotFoundException("Invalid filename");

				if (f.exists())
					NotifyUtil.setText("Skipped: %s already downloaded.", filename);
				else
					downloadFileFromURL(f, new URL(finalUrl));
			} catch (FileNotFoundException e) {
				OneClientLogging.error(e);
			}
		}
	}

	public static void downloadFileFromURL(File f, URL url) throws IOException {
		FileUtils.copyURLToFile(url, FileUtil.createFile(f));
	}

}
