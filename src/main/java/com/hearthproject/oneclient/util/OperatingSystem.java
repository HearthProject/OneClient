package com.hearthproject.oneclient.util;

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
}
