package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.IImporter;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import javafx.scene.image.Image;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CurseImporter implements IImporter {

	private final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
	private final String projectID;
	private final URL url;
	private AsyncTask<CurseProject> data;

	public CurseImporter(String projectID) {
		this.projectID = projectID;
		url = Curse.getProjectURL(projectID);
		data = new AsyncTask<>(() -> JsonUtil.read(url, CurseProject.class));
		service.execute(data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Instance create() {
		String name = data.map(CurseProject::getName);
		List<String> authors = data.map(CurseProject::getAuthors);

		List<CurseProject.Category> categories = data.map(CurseProject::getCategories);
		String websiteUrl = data.map(CurseProject::getWebSiteURL);
		if (name == null)
			return null;
		Instance instance = new Instance(name, url.toString(), new CurseInstaller(getFiles()), Pair.of("authors", authors.stream().collect(Collectors.joining("\n"))), Pair.of("websiteUrl", websiteUrl), Pair.of("categories", categories));
		instance.setImage(getImage());
		return instance;
	}

	private Image getImage() {
		URL url = data.map(CurseProject::getIcon);
		File file = new File(Constants.ICONDIR, data.map(CurseProject::getName) + ".png");
		FileUtil.downloadFromURL(url, file);
		return ImageUtil.openCachedImage(file);
	}

	public List<CurseProject.CurseFile> getFiles() {
		CurseProject.CurseFile[] files = JsonUtil.read(Curse.getProjectFilesURL(projectID), CurseProject.CurseFile[].class);
		if (files != null) {
			List<CurseProject.CurseFile> list = Lists.newArrayList(files);
			Collections.sort(list);
			return list;
		}
		return null;
	}

}
