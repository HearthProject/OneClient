package com.hearthproject.oneclient.util.files;

import java.io.File;

public class FileUtil {
	public static File findDirectory(File path, String dir) {
		File file = new File(path, dir);
		if (!file.exists())
			file.mkdir();
		return file;
	}
}
