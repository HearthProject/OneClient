package com.hearthproject.oneclient.util.curse;

import com.google.gson.Gson;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import javafx.application.Platform;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.*;
import java.net.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Taken from: https://github.com/Vazkii/CMPDL/tree/master/src/vazkii/cmpdl + changed a bit
public class CursePackInstaller {
	public final Gson GSON_INSTANCE = new Gson();

	public final Pattern FILE_NAME_URL_PATTERN = Pattern.compile(".*?/([^/]*)$");


	public List<String> missingMods = null;

	public void downloadFromURL(String url, String version, Instance instance) throws Exception {

		if (url.contains("feed-the-beast.com") && version.equals("latest")) {
			log("WARNING: For modpacks hosted in the FTB site, you need to provide a version, \"latest\" will not work!");
			log("To find the version number to insert in the Curse File ID field, click the latest file on the sidebar on the right of the modpack's page.");
			log("The number you need to input is the number at the end of the URL.");
			log("For example, if you wanted to download https://www.feed-the-beast.com/projects/ftb-presents-skyfactory-3/files/2390075");
			log("Then you would use 2390075 as the Curse File ID. Do not change the Modpack URL. Change that and click Download again to continue.");
			return;
		}

		missingMods = new ArrayList<String>();
		log("~ Starting magical modpack download sequence ~");
		log("Input URL: " + url);
		log("Input Version: " + version);

		String packUrl = url;
		if (packUrl.endsWith("/"))
			packUrl = packUrl.replaceAll(".$", "");

		String packVersion = version;
		if (version == null || version.isEmpty())
			packVersion = "latest";

		String fileUrl;
		if (packVersion.equals("latest"))
			fileUrl = packUrl + "/files/latest";
		else
			fileUrl = packUrl + "/files/" + packVersion + "/download";

		String finalUrl = getLocationHeader(fileUrl);
		log("File URL: " + fileUrl);
		log("Final URL: " + finalUrl);

		Matcher matcher = FILE_NAME_URL_PATTERN.matcher(finalUrl);
		if (matcher.matches()) {
			String filename = matcher.group(1);
			log("Modpack filename is " + filename);

			File unzippedDir = setupModpackMetadata(filename, finalUrl);
			Manifest manifest = getManifest(unzippedDir);
			instance.minecraftVersion = manifest.minecraft.version;
			instance.modLoader = "forge";
			instance.modLoaderVersion = manifest.getForgeVersion();
			instance.name = manifest.name;
			InstallingController.controller.setTitleText("Downloading " + instance.name);
			System.out.println(instance.modLoaderVersion);
			File minecraftDir = instance.getDirectory();

			downloadModpackFromManifest(minecraftDir, manifest);
			copyOverrides(manifest, unzippedDir, minecraftDir);

			log("And we're done!");
			log("Output Path: " + minecraftDir);

			if (!missingMods.isEmpty()) {
				log("");
				log("WARNING: Some mods could not be downloaded. Either the specific versions were taken down from "
					+ "public download on CurseForge, or there were errors in the download.");
				log("The missing mods are the following:");
				for (String mod : missingMods)
					log(" - " + mod);
				log("");
				log("If these mods are crucial to the modpack functioning, try downloading the server version of the pack "
					+ "and pulling them from there.");
			}
			missingMods = null;

			log("################################################################################################");

			log("Done");

		}
	}

	public File setupModpackMetadata(String filename, String url) throws IOException, ZipException {
		log("Setting up Metadata");

		File homeDir = getTempDir();

		File retDir = new File(homeDir, filename);
		log("Modpack temporary directory is " + retDir);
		if (!retDir.exists()) {
			log("Directory doesn't exist, making it now");
			retDir.mkdir();
		}

		String zipName = filename;
		if (!zipName.endsWith(".zip"))
			zipName = zipName + ".zip";

		String retPath = retDir.getAbsolutePath();
		retDir.deleteOnExit();

		log("Downloading zip file " + zipName);
		log("Downloading Modpack .zip");
		File f = new File(retDir, zipName);
		downloadFileFromURL(f, new URL(url));

		log("Unzipping Modpack Download");
		log("Unzipping file");
		ZipFile zip = new ZipFile(f);
		zip.extractAll(retPath);

		log("Done unzipping");

		return retDir;
	}

	public File getTempDir() {
		return new File(Constants.TEMPDIR, "curseDownload");
	}

	public Manifest getManifest(File dir) throws IOException {
		log("Parsing Manifest");
		File f = new File(dir, "manifest.json");
		if (!f.exists())
			throw new IllegalArgumentException("This modpack has no manifest");

		log("Parsing Manifest");
		Manifest manifest = GSON_INSTANCE.fromJson(new FileReader(f), Manifest.class);

		return manifest;
	}

	public File downloadModpackFromManifest(File outputDir, Manifest manifest) throws IOException, URISyntaxException {
		int total = manifest.files.size();

		log("Downloading modpack from Manifest");
		log("Manifest contains " + total + " files to download");

		File modsDir = new File(outputDir, "mods");
		if (!modsDir.exists())
			modsDir.mkdir();

		int left = total;
		for (Manifest.FileData f : manifest.files) {
			left--;
			downloadFile(f, modsDir, left, total);
		}

		log("Mod downloads complete");

		return outputDir;
	}

	public void copyOverrides(Manifest manifest, File tempDir, File outDir) throws IOException {
		log("Copying modpack overrides");
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
		log("Done copying overrides");
	}

	public void downloadFile(Manifest.FileData file, File modsDir, int remaining, int total) throws IOException, URISyntaxException {
		log("Downloading " + file);
		log("File: " + file + " (" + (total - remaining) + "/" + total + ")");
		InstallingController.controller.setProgress(total - remaining, total);
		log("Acquiring Info");

		String baseUrl = "http://minecraft.curseforge.com/projects/" + file.projectID;
		log("Project URL is " + baseUrl);

		String projectUrl = getLocationHeader(baseUrl);
		projectUrl = projectUrl.replaceAll("\\?cookieTest=1", "");
		String fileDlUrl = projectUrl + "/files/" + file.fileID + "/download";
		log("File download URL is " + fileDlUrl);

		String finalUrl = getLocationHeader(fileDlUrl);
		Matcher m = FILE_NAME_URL_PATTERN.matcher(finalUrl);
		if (!m.matches())
			throw new IllegalArgumentException("Mod file doesn't match filename pattern");

		String filename = m.group(1);
		filename = URLDecoder.decode(filename, "UTF-8");
		InstallingController.controller.setDetailText("Downloading " + filename);
		if (filename.endsWith("cookieTest=1")) {
			log("Missing file! Skipping it");
			missingMods.add(finalUrl);
		} else {

			log("Downloading " + filename);

			File f = new File(modsDir, filename);
			try {
				if (filename.equals("download"))
					throw new FileNotFoundException("Invalid filename");

				if (f.exists())
					log("This file already exists. No need to download it");
				else
					downloadFileFromURL(f, new URL(finalUrl));
				log("Downloaded! " + remaining + "/" + total + " remaining");
			} catch (FileNotFoundException e) {
				log("Error: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
				log("This mod will not be downloaded. If you need the file, you'll have to get it manually:");
				log(finalUrl);
				missingMods.add(finalUrl);
			}
		}
	}

	public String getLocationHeader(String location) throws IOException, URISyntaxException {
		URI uri = new URI(location);
		HttpURLConnection connection = null;
		String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36";
		for (; ; ) {
			URL url = uri.toURL();
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", userAgent);
			connection.setInstanceFollowRedirects(false);
			String redirectLocation = connection.getHeaderField("Location");
			if (redirectLocation == null)
				break;

			// This gets parsed out later
			redirectLocation = redirectLocation.replaceAll("\\%20", " ");

			if (redirectLocation.startsWith("/"))
				uri = new URI(uri.getScheme(), uri.getHost(), redirectLocation, uri.getFragment());
			else {
				url = new URL(redirectLocation);
				uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			}
		}

		return uri.toString();
	}

	public void downloadFileFromURL(File f, URL url) throws IOException {

		System.out.println(f + ":" + url);
		if (!f.exists()){
			if(!f.getParentFile().exists()){
				f.getParentFile().mkdirs();
			}
			f.createNewFile();
		}

		try (InputStream instream = url.openStream(); FileOutputStream outStream = new FileOutputStream(f)) {
			byte[] buff = new byte[4096];

			int i;
			while ((i = instream.read(buff)) > 0)
				outStream.write(buff, 0, i);
		}
	}

	public void log(String s) {
		OneClientLogging.log(s);
	}

}
