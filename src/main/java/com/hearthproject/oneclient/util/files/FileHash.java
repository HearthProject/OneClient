package com.hearthproject.oneclient.util.files;

import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FileHash {

	private String file;
	private String hash;

	public FileHash(File file) {
		this.file = file.toString();
		try {
			this.hash = getFileChecksum(file);
		} catch (IOException e) {
			OneClientLogging.error("{} {}", file, e);
		}
	}

	public boolean matches(File file) {
		try {
			return getFileChecksum(file).equals(this.hash);
		} catch (IOException e) {
			OneClientLogging.error("{} {}", file, e);
		}
		return false;
	}

	public File getFile() {
		try {
			URI u = new URI(file.trim().replaceAll("\\u0020", "%20"));
			return new File(u.getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getFilePath() {
		return file;
	}

	public String getHash() {
		return hash;
	}

	private static String getFileChecksum(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		String md5 = DigestUtils.md5Hex(stream);
		stream.close();
		return md5;
	}
}
