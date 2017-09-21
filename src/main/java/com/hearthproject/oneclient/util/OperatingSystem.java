package com.hearthproject.oneclient.util;

import com.hearthproject.oneclient.util.logging.OneClientLogging;

import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.URI;

//Based of the code from fabric-loom found here: https://github.com/FabricMC/fabric-loom
public class OperatingSystem {

	public static final String SYSTEM_ARCH = System.getProperty("os.arch").equals("64") ? "64" : "32";

	public static File getPrograms() {
		return OperatingSystem.isWindows() ? new File(System.getenv("%ProgramW6432%")) : new File("/usr/lib");
	}

	public static boolean isWindows() {
		return getOS().equalsIgnoreCase("windows");
	}

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

	public static File getApplicationDataDirectory() {
		String userHome = System.getProperty("user.home", ".");
		if (getOS().equals("windows")) {
			String applicationData = System.getenv("APPDATA");
			if (applicationData != null) {
				return new File(applicationData, "." + "OneClient" + '/');
			} else {
				return new File(userHome, '.' + "OneClient" + '/');
			}
		} else if (getOS().equals("osx")) {
			return new File(userHome, "Library/Application Support/OneClient");
		} else if (getOS().equals("linux")) {
			return new File(userHome, '.' + "OneClient" + '/');
		}
		return new File(userHome, "OneClient" + '/');
	}

	public static boolean is64Bit() {
		return System.getProperty("sun.arch.data.model").contains("64");
	}

	public static void withDesktop(MiscUtil.ThrowingConsumer<Desktop> consumer) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN)) {
				new Thread(() -> {
					try {
						consumer.accept(desktop);
					} catch (Exception e) {
						OneClientLogging.error(e);
					}
				}).start();
			}
		}
	}

	public static void openWithSystem(File file) {
		withDesktop(desktop -> desktop.open(file));
	}

	public static void browseURI(String uri) {
		withDesktop(desktop -> desktop.browse(new URI(uri)));
	}

	public static long getOSTotalMemory() {
		return getOSMemory("getTotalPhysicalMemorySize", "Could not get RAM Value");
	}

	public static long getOSFreeMemory() {
		return getOSMemory("getFreePhysicalMemorySize", "Could not get free RAM Value");
	}

	//Seems to be the safest way to get memory ifo without it exploding.
	private static long getOSMemory(String methodName, String warning) {
		long ram = 0;
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		Method m;
		try {
			m = operatingSystemMXBean.getClass().getDeclaredMethod(methodName);
			m.setAccessible(true);
			Object value = m.invoke(operatingSystemMXBean);
			if (value != null) {
				ram = Long.valueOf(value.toString()) / 1024 / 1024;
			} else {
				OneClientLogging.logger.warn(warning);
				ram = 1024;
			}
		} catch (Exception e) {
			OneClientLogging.error(e);
		}

		return ram;
	}

}
