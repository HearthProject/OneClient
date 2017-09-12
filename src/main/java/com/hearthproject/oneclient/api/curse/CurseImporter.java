package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.HearthInstance;
import com.hearthproject.oneclient.api.IImporter;
import com.hearthproject.oneclient.api.Info;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.json.JsonUtil;

import java.io.File;
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
	public HearthInstance create() {
		String name = data.getName();
		CurseProject.CurseFile file = getLatestFile();
		List<String> authors = data.getAuthors();

		new Thread(() -> FileUtil.downloadFromURL(data.getIcon(), new File(Constants.ICONDIR, name + ".png"))).start();

		if (name == null || file == null)
			return null;
		return new HearthInstance(name, file.getFileName(), gameVersion, url.toString(), new CurseInstaller(file), new Info("authors", authors));
	}

	public CurseProject.CurseFile getLatestFile() {
		CurseProject.CurseFile[] files = JsonUtil.read(Curse.getProjectFilesURL(projectID), CurseProject.CurseFile[].class);
		return Iterables.getLast(Lists.newArrayList(files), null);
	}

}
