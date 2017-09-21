package com.hearthproject.oneclient.fx.nodes;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JavaDialog extends TableDialog<JavaDialog.Java> {

	public JavaDialog() {
		super(findJVMS());
	}

	private static Predicate<String> isJavaPath = path -> path.endsWith("/bin/java");

	//TODO check github todo for this
	public static List<Java> findJVMS() {
		List<Java> javasPath = Arrays.stream(System.getenv("PATH").split(";")).filter(isJavaPath).map(Java::new).collect(Collectors.toList());
		return null;
	}

	public static class Java {
		private File directory;

		public Java(File directory) {
			this.directory = directory;
		}

		public Java(String directory) {
			this(new File(directory));
		}

		public File getDirectory() {
			return directory;
		}
	}
}
