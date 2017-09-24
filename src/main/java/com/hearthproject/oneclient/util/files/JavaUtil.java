package com.hearthproject.oneclient.util.files;

import com.google.common.collect.Sets;
import com.hearthproject.oneclient.util.OperatingSystem;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaUtil {

	public static Set<JavaInstall> getAvailableInstalls() {
		PotentialPaths potential = new PotentialPaths() {
			@Override
			Set<JavaInstall> findJavaPaths() { return Sets.newHashSet(); }
		};
		switch (OperatingSystem.getOS()) {
			case "windows":
				potential = new PotentialWindows();
				break;
			case "linux":
				potential = new PotentialLinux();
				break;
			case "osx":
				potential = new PotentialMac();
				break;
		}
		return potential.findJavaPaths().stream().filter(install -> install.getPath().exists()).collect(Collectors.toSet());
	}

	public static JavaInstall getDefault() {
		String java = "";
		switch (OperatingSystem.getOS()) {
			case "windows":
				java = "/bin/java.exe";
				break;
			case "linux":
			case "osx":
				java = "/bin/java";
				break;
		}
		return new JavaInstall(System.getProperty("java.home") + java);
	}

	public static class JavaInstall {
		public String path, id, arch;

		public JavaInstall(String path, String id, String arch) {
			this.path = path;
			this.id = id;
			this.arch = arch;
		}

		public JavaInstall(String path) {
			this(path, "unknown", "unknown");
		}

		@Override
		public boolean equals(Object a) {
			return a instanceof JavaInstall && path.equals(((JavaInstall) a).path);
		}

		@Override
		public int hashCode() {
			return path.hashCode() ^ id.hashCode() ^ arch.hashCode();
		}

		public File getPath() {
			return new File(path);
		}

		@Override
		public String toString() {
			return path;
		}

	}

	static abstract class PotentialPaths {
		abstract Set<JavaInstall> findJavaPaths();
	}

	static class PotentialWindows extends PotentialPaths {

		//TODO registry entries

		@Override
		Set<JavaInstall> findJavaPaths() {
			return Sets.newHashSet(
				getDefault(),
				new JavaInstall("C:/Program Files/Java/jre8/bin/javaw.exe"),
				new JavaInstall("C:/Program Files/Java/jre7/bin/javaw.exe"),
				new JavaInstall("C:/Program Files/Java/jre6/bin/javaw.exe"),
				new JavaInstall("C:/Program Files (x86)/Java/jre8/bin/javaw.exe"),
				new JavaInstall("C:/Program Files (x86)/Java/jre7/bin/javaw.exe"),
				new JavaInstall("C:/Program Files (x86)/Java/jre6/bin/javaw.exe")
			);
		}
	}

	static class PotentialLinux extends PotentialPaths {
		private FileFilter filter = file -> file.isDirectory() && !Files.isSymbolicLink(file.toPath());

		private Set<JavaInstall> scanJavaDir(File file) {
			File[] dirs = file.listFiles(filter);
			Set<JavaInstall> installs = Sets.newHashSet();
			if (dirs != null) {
				for (File dir : dirs) {
					installs.add(new JavaInstall(new File(dir, "jre/bin/java").toString()));
					installs.add(new JavaInstall(new File(dir, "bin/java").toString()));
				}
			}
			return installs;
		}

		@Override
		Set<JavaInstall> findJavaPaths() {
			Set<JavaInstall> installs = Sets.newHashSet(getDefault());
			//From MultiMC
			// oracle RPMs
			installs.addAll(scanJavaDir(new File("/usr/java")));
			// general locations used by distro packaging
			installs.addAll(scanJavaDir(new File("/usr/lib/jvm")));
			installs.addAll(scanJavaDir(new File("/usr/lib32/jvm")));
			// javas stored in MultiMC's folder
			installs.addAll(scanJavaDir(new File("java")));
			return installs;
		}
	}

	static class PotentialMac extends PotentialPaths {

		private FileFilter filter = File::isDirectory;

		private Set<JavaInstall> scanJavaDir(File file, String... subdirs) {
			File[] dirs = file.listFiles(filter);
			Set<JavaInstall> installs = Sets.newHashSet();
			if (dirs != null) {
				for (File dir : dirs) {
					for (String sub : subdirs) {
						installs.add(new JavaInstall(new File(dir, sub).toString()));
					}
				}
			}
			return installs;
		}

		@Override
		Set<JavaInstall> findJavaPaths() {
			Set<JavaInstall> installs = Sets.newHashSet(
				getDefault(),
				new JavaInstall("/Applications/Xcode.app/Contents/Applications/Application Loader.app/Contents/MacOS/itms/java/bin/java"),
				new JavaInstall("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"),
				new JavaInstall("/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java")
			);
			//TODO more
			return installs;
		}
	}

}
