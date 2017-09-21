package com.hearthproject.oneclient.api.curse;

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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CurseImporter implements IImporter {

	private final static ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
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
		String authors = data.map(CurseProject::getAuthors).stream().map(CurseProject.Author::getName).collect(Collectors.joining(", "));
		String gameVersions = data.map(CurseProject::getLatestFiles).stream().flatMap(file -> file.getGameVersion().stream()).distinct().collect(Collectors.joining(", "));
		List<CurseProject.Category> categories = data.map(CurseProject::getCategories);
		String websiteUrl = data.map(CurseProject::getWebSiteURL);
		if (name == null)
			return null;

		Instance instance = new Instance(name, url.toString(), new CurseInstaller(data.getIfPresent()),
			new Instance.Info("popularity", data.map(CurseProject::getPopularityScore)),
			new Instance.Info("authors", authors),
			new Instance.Info("websiteUrl", websiteUrl),
			new Instance.Info("categories", categories),
			new Instance.Info("downloads", data.map(CurseProject::getDownloads)),
			new Instance.Info("gameVersions", gameVersions),
			new Instance.Info("summary", data.map(CurseProject::getSummary))
		);
		instance.setImage(getImage());
		return instance;
	}

	private Image getImage() {
		URL url = data.map(CurseProject::getIcon);
		String name = null;
		try {
			name = URLEncoder.encode(data.map(CurseProject::getName), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		File file = new File(Constants.ICONDIR, name + ".png");
		FileUtil.downloadFromURL(url, file);
		return ImageUtil.openCachedImage(file);
	}

}
