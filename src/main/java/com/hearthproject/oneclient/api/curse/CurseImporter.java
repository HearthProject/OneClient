package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.HeathInstance;
import com.hearthproject.oneclient.api.IImporter;
import com.hearthproject.oneclient.api.Info;
import com.hearthproject.oneclient.json.JsonUtil;

import java.net.URL;
import java.util.List;

public class CurseImporter implements IImporter {

	private final String projectID;
	private final String gameVersion;
	private final URL url;
	private CurseProject data;

	public CurseImporter(String projectID, String gameVersion) {
		this.projectID = projectID;
		this.gameVersion = gameVersion;
		url = Curse.getProjectURL(projectID);
		data = JsonUtil.read(url, CurseProject.class);
	}

	@Override
	public HeathInstance create() {
		String name = data.getName();
		CurseProject.CurseFile file = getLatestFile();
		List<String> authors = data.getAuthors();

		if (name == null || file == null)
			return null;
		return new HeathInstance(name, file.getFileName(), gameVersion, url.toString(), new CurseInstaller(file), new Info("authors", authors));
	}

	public CurseProject.CurseFile getLatestFile() {
		CurseProject.CurseFile[] files = JsonUtil.read(Curse.getProjectFilesURL(projectID), CurseProject.CurseFile[].class);
		return Iterables.getLast(Lists.newArrayList(files), null);
	}

}
