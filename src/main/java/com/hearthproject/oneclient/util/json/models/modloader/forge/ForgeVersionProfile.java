package com.hearthproject.oneclient.util.json.models.modloader.forge;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.hearthproject.oneclient.Constants;

import java.io.File;
import java.util.List;

public class ForgeVersionProfile {

	public String type, mainClass, inheritsFrom, jar, minecraftArguments;

	public List<Library> libraries;

	public class Library {
		public String name;
		public boolean serverreq = true;
		public boolean clientreq = true;
		public String url = Constants.LIBRARIES_BASE;
		public List<String> checksums;

		public String getURL() {
			if (url == null) {
				url = Constants.LIBRARIES_BASE;
			}
			String[] pts = Iterables.toArray(Splitter.on(':').split(name), String.class);
			String domain = pts[0];
			String libNamename = pts[1];

			int last = pts.length - 1;
			int idx = pts[last].indexOf('@');
			if (idx != -1) {
				pts[last] = pts[last].substring(0, idx);
			}

			String version = pts[2];
			String classifier = null;
			String ext = "jar";
			if (pts.length > 3) {
				classifier = pts[3];
			}
			if (domain.equals("net.minecraftforge") && libNamename.equals("forge")) {
				classifier = "universal";
			}

			String file = libNamename + '-' + version;
			if (classifier != null)
				file += '-' + classifier;
			file += '.' + ext;

			String path = domain.replace('.', '/') + '/' + libNamename + '/' + version + '/' + file;
			return url + path;
		}

		public File getFile(File dir) {
			String[] parts = this.name.split(":", 3);
			return new File(dir, parts[0].replace(".", File.separator) + File.separator + parts[1] + File.separator + parts[2] + File.separator + parts[1] + "-" + parts[2] + ".jar");
		}

	}
}
