package com.hearthproject.oneclient.util.curse;

import com.google.gson.Gson;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
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
import java.nio.file.FileAlreadyExistsException;
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

		//		if (url.contains("feed-the-beast.com") && version.equals("latest")) {
		//			log("WARNING: For modpacks hosted in the FTB site, you need to provide a version, \"latest\" will not work!");
		//			log("To find the version number to insert in the Curse File ID field, click the latest file on the sidebar on the right of the modpack's page.");
		//			log("The number you need to input is the number at the end of the URL.");
		//			log("For example, if you wanted to download https://www.feed-the-beast.com/projects/ftb-presents-skyfactory-3/files/2390075");
		//			log("Then you would use 2390075 as the Curse File ID. Do not change the Modpack URL. Change that and click Download again to continue.");
		//			return;
		//		}

		missingMods = new ArrayList<>();
		log("Curse element downloader created by Vazkii");
		log("https://github.com/Vazkii/CMPDL");
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
		log("Modpack filename is " + zipFile.getName());
		File unzippedDir = unzipPack(getTempPackDir(zipFile), zipFile);
		Manifest manifest = getManifest(unzippedDir);
		instance.minecraftVersion = manifest.minecraft.version;
		instance.modLoader = "forge";
		instance.modLoaderVersion = manifest.getForgeVersion();
		instance.name = manifest.name;
		instance.curseVersion = manifest.version;
		int i = 1;
		while (!InstanceManager.isValid(instance)) {
			instance.name = manifest.name + "(" + i + ")";
		}
		InstallingController.controller.setTitleText("Downloading " + manifest.name);
		OneClientTracking.sendRequest("curse/install/" + manifest.name + "/" + manifest.version);
		File minecraftDir = instance.getDirectory();

		downloadModpackFromManifest(minecraftDir, manifest);
		copyOverrides(manifest, unzippedDir, minecraftDir);

		log("Done downloading element " + manifest.name);
		InstallingController.close();

		//		if (!missingMods.isEmpty()) {
		//			log("");
		//			log("WARNING: Some mods could not be downloaded. Either the specific versions were taken down from "
		//				+ "public download on CurseForge, or there were errors in the download.");
		//			log("The missing mods are the following:");
		//			for (String mod : missingMods)
		//				log(" - " + mod);
		//			log("");
		//			log("If these mods are crucial to the modpack functioning, try downloading the server version of the element "
		//				+ "and pulling them from there.");
		//		}
		missingMods = null;
	}

	public File downloadPackZip(String pack, URL url) throws IOException {
		File packDir = getTempPackDir(pack);
		String zipName = pack;
		if (!zipName.endsWith(".zip"))
			zipName = zipName + ".zip";
		log("Downloading zip file " + zipName);
		File zipFile = new File(packDir, zipName);
		downloadFileFromURL(zipFile, url);
		return zipFile;
	}

	public File unzipPack(File dir, File packZip) throws IOException, ZipException {
		log("Unzipping Modpack Download");
		ZipFile zip = new ZipFile(packZip);
		zip.extractAll(dir.toString());
		return dir;
	}

	public File getTempDir() {
		return new File(Constants.TEMPDIR, "curseDownload");
	}

	public File getTempPackDir(String pack) {
		File home = getTempDir();
		File retDir = new File(home, FilenameUtils.removeExtension(pack));
		if (!retDir.exists()) {
			retDir.mkdir();
		}
		return retDir;
	}

	public File getTempPackDir(File zipFile) {
		return getTempPackDir(zipFile.getName());
	}

	public Manifest getManifest(File dir) throws IOException {
		log("Parsing Manifest");
		File f = new File(dir, "manifest.json");
		if (!f.exists())
			throw new IllegalArgumentException("This modpack has no manifest");

		Manifest manifest = GSON_INSTANCE.fromJson(new FileReader(f), Manifest.class);

		return manifest;
	}

	public int left;

	public File downloadModpackFromManifest(File outputDir, Manifest manifest) throws IOException, URISyntaxException {
		int total = manifest.files.size();

		log("Downloading modpack from Manifest");
		log("Manifest contains " + total + " files to download");

		File modsDir = new File(outputDir, "mods");
		if (!modsDir.exists())
			modsDir.mkdir();

		left = total;

		manifest.files.parallelStream().forEach(f -> {
			left--;

			try {
				downloadFile(f, modsDir, left, total);
			} catch (IOException | URISyntaxException e) {
				OneClientLogging.logger.error(e);
			}
		});

		log("Mod downloads complete");

		return outputDir;
	}

	public void copyOverrides(Manifest manifest, File tempDir, File outDir) throws IOException {
		log("Copying modpack overrides");
		File overridesDir = new File(tempDir, manifest.overrides);

		Files.walk(overridesDir.toPath()).forEach(path -> {
			try {
				log("Override: " + path.getFileName());
				Files.copy(path, Paths.get(path.toString().replace(overridesDir.toString(), outDir.toString())));
			} catch (IOException e) {
				if (!(e instanceof FileAlreadyExistsException))
					log("Error copying " + path.getFileName() + ": " + e.getMessage() + ", " + e.getClass());
			}
		});
	}

	public void downloadFile(Manifest.FileData file, File modsDir, int remaining, int total) throws IOException, URISyntaxException {
		InstallingController.controller.setProgress(total - remaining, total);
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
		log("Downloading " + filename);

		if (filename.endsWith("cookieTest=1")) {
			log("Missing file! Skipping it");
			missingMods.add(finalUrl);
		} else {
			File f = new File(modsDir, filename);
			try {
				if (filename.equals("download"))
					throw new FileNotFoundException("Invalid filename");

				if (f.exists())
					log("This file already exists. No need to download it");
				else
					downloadFileFromURL(f, new URL(finalUrl));
			} catch (FileNotFoundException e) {
				log("Error: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
				log("This mod will not be downloaded. If you need the file, you'll have to get it manually:");
				log(finalUrl);
				missingMods.add(finalUrl);
			}
		}
	}



	public void downloadFileFromURL(File f, URL url) throws IOException {
		if (!f.exists()) {
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			f.createNewFile();
		}
		FileUtils.copyURLToFile(url, f);
	}

	//Find alternative to this
	@Deprecated
	public void log(String s) {
		OneClientLogging.logger.info(s);
		InstallingController.controller.setDetailText(s);
	}

}
