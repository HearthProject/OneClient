package com.hearthproject.oneclient;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class Constants {

	public static final File TEMPDIR = new File(getRunDir(), "temp");
	public static final File INSTANCEDIR = new File(getRunDir(), "instances");
	public static final File LOGFILE = new File(getRunDir(), "log.txt");
	public static final File TEMP_UPDATE = new File(getRunDir(), "temp_update.jar");

	public static final String LIBRARIES_BASE = "https://libraries.minecraft.net/";
	public static final String RESOURCES_BASE = "http://resources.download.minecraft.net/";
	public static final String MAVEN_CENTRAL_BASE = "http://central.maven.org/maven2/";

	public static String[] INITIALIZE_DIRS = new String[]{"configs","mods"};

	public static boolean CUSTOM_RUN = false;

	public static File getRunDir() {
		String runDir = System.getProperty("OneClient.runDir", "");
		if (!runDir.isEmpty()) {
			CUSTOM_RUN = true;
			return new File(runDir);
		}
		return new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "OneClient");
	}

	public static String getVersion() {
		return Constants.class.getPackage().getImplementationVersion();
	}

}