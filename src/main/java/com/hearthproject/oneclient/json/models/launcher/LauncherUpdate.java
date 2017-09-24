package com.hearthproject.oneclient.json.models.launcher;

import java.util.Arrays;

public class LauncherUpdate implements Comparable<LauncherUpdate> {

	public String latestVersion;
	public String downloadUrl;
	public String portableDownloadUrl;
	public boolean required;

	public LauncherUpdate(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public int[] getVersion() {
		if (this.latestVersion == null)
			return new int[0];
		String[] a = this.latestVersion.split("\\.");
		return Arrays.stream(a).mapToInt(Integer::parseInt).toArray();
	}

	@Override
	public int compareTo(LauncherUpdate o) {
		int[] versionA = this.getVersion(), versionB = o.getVersion();
		int length = Math.max(versionA.length, versionB.length);
		for (int i = 0; i < length; i++) {
			int a = i < versionA.length ? versionA[i] : 0;
			int b = i < versionB.length ? versionB[i] : 0;
			if (a < b)
				return -1;
			if (b > a)
				return 1;
		}
		return 0;
	}
}
