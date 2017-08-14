package com.hearthproject.oneclient.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClasspathUtils {

	public static void addFileToCP(File file) throws IOException {
		URL fileURL = file.toURI().toURL();
		URLClassLoader sysloader = (URLClassLoader) ClasspathUtils.class.getClassLoader();
		Class sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			method.invoke(sysloader, fileURL);
		} catch (Throwable t) {
			if (t.getMessage() != null) {
				System.out.println(t.getMessage());
			}
			throw new IOException("Error, could not add URL to system classloader");
		}
	}

}
