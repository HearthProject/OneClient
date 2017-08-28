package com.hearthproject.oneclient.json.models.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.util.curse.CurseUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Instance {

	public String name;

	public String minecraftVersion;

	public String modLoader;

	public String modLoaderVersion;

	public String icon;

	public long lastLaunch;

	public String curseURL;

	public String curseVersion;

	public Instance(String name) {
		this.name = name;
		icon = "";
	}

	public File getDirectory() {
		return new File(Constants.INSTANCEDIR, name);
	}

	public File getIcon() {
		if (icon == null || icon.isEmpty()) {
			return null;
		}
		return new File(getDirectory(), icon);
	}

	public String getZipURL() throws IOException, URISyntaxException {
		String packUrl = curseURL;
		if (packUrl.endsWith("/"))
			packUrl = packUrl.replaceAll(".$", "");

		String fileUrl;
		if (curseVersion.equals("latest"))
			fileUrl = packUrl + "/files/latest";
		else
			fileUrl = packUrl + "/files/" + curseVersion + "/download";
		return CurseUtils.getLocationHeader(fileUrl);
	}

}
