package com.hearthproject.oneclient.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MiscUtil {

	public static boolean checksumEquals(File file, String checksum) {
		if (file == null || !file.exists()) {
			return false;
		}
		try {
			HashCode hash = Files.hash(file, Hashing.sha1());
			StringBuilder builder = new StringBuilder();
			for (Byte hashBytes : hash.asBytes()) {
				builder.append(Integer.toString((hashBytes & 0xFF) + 0x100, 16).substring(1));
			}
			return builder.toString().equals(checksum);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean checksumEquals(File file, List<String> checksum) {
		if (file == null || !file.exists()) {
			return false;
		}
		try {
			HashCode hash = Files.hash(file, Hashing.sha1());
			StringBuilder builder = new StringBuilder();
			for (Byte hashBytes : hash.asBytes()) {
				builder.append(Integer.toString((hashBytes & 0xFF) + 0x100, 16).substring(1));
			}
			return checksum.contains(builder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int getResponseCode(URL url) throws IOException {
		HttpURLConnection huc = (HttpURLConnection)url.openConnection();
		huc.setRequestMethod("GET");
		huc.connect();
		int code = huc.getResponseCode();
		return code;
	}
}
