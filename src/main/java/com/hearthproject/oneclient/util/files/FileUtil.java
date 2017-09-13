package com.hearthproject.oneclient.util.files;

import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class FileUtil {

	public static File createDirectory(File path) {
		if (path == null)
			return null;

		if (!path.exists())
			path.mkdirs();
		return path;
	}

	public static File findDirectory(File path, String dir) {
		return createDirectory(getDirectory(path, dir));
	}

	public static File getDirectory(File path, String dir) {
		try {
			return new File(path, URLEncoder.encode(dir, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static File createFile(File file) {
		if (!file.exists()) {
			createDirectory(new File(file.getParent()));
			try {
				file.createNewFile();
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
		}
		return file;
	}

	public static File findFile(File parent, String file) {
		File f = new File(parent, file);
		return createFile(f);
	}

	public static void downloadFromURL(String url, File location) {
		try {
			downloadFromURL(new URL(url), location);
		} catch (MalformedURLException e) {
			OneClientLogging.error(e);
		}
	}

	public static void downloadFromURL(URL url, File location) {
		try {
			FileUtils.copyURLToFile(url, location);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
	}

	public static InputStream getResource(String resource) {
		if (resource == null)
			return null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader.getResourceAsStream(resource);
	}

	public static boolean isDirectoryEmpty(File dir) {
		return dir.isDirectory() && (dir.listFiles() == null || dir.listFiles().length == 0);
	}

	public static void upload() {


	}
}
