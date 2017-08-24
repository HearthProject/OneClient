package com.hearthproject.oneclient.util;

import java.awt.*;
import java.io.File;
import java.io.IOException;

//Based of the code from fabric-loom found here: https://github.com/FabricMC/fabric-loom
public class OperatingSystem {

	public static final String SYSTEM_ARCH = System.getProperty("os.arch").equals("64") ? "64" : "32";

	public static String getOS() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			return "windows";
		} else if (osName.contains("mac")) {
			return "osx";
		} else {
			return "linux";
		}
	}

	public static String getArch() {
		if (is64Bit()) {
			return "64";
		} else {
			return "32";
		}
	}

	public static String getJavaDelimiter() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			return ";";
		} else if (osName.contains("mac")) {
			return ":";
		} else {
			return ":";
		}
	}

	public static boolean is64Bit() {
		return System.getProperty("sun.arch.data.model").contains("64");
	}

	public static void openWithSystem(File file) {
		if(Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN)) {
				new Thread( () -> {
					try {
						desktop.open(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			}
		}
	}

}
