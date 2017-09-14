package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.curse.CurseMetaUtils;

public class CurseMod extends CurseTile {
	private final Instance instance;

	public CurseMod(Instance instance, CurseElement element) {
		super(element);
		this.instance = instance;
	}

	@Override
	public void install() {
		new Thread(() -> {
			String version = instance.getGameVersion();
			int projectID = element.getID();
			CurseMetaUtils.ProjectFile file = CurseMetaUtils.getLatest(projectID, version);

			//			File modsDir = FileUtil.findDirectory(instance.getDirectory(), "mods");
			//			try {
			//				FileData data = file.getFileData(projectID);
			//				CursePackInstaller.downloadFile(data, modsDir, 1, 2);
			//				MiscUtil.runLaterIfNeeded(() -> {
			//					instance.getManifest().files.add(data);
			//					Main.mainController.currentContent.refresh();
			//					instance.save();
			//				});
			//				NotifyUtil.clear();
			//
			//			} catch (IOException | URISyntaxException e) {
			//				e.printStackTrace();
			//			}

		}).start();
	}
}
