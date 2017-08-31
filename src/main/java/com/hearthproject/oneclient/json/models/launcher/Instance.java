package com.hearthproject.oneclient.json.models.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

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
		return FileUtil.findDirectory(Constants.INSTANCEDIR, name);
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

	public void delete() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Are you sure you want to delete the pack");
		alert.setContentText("This will remove all mods and worlds, this cannot be undone!");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				ContentPanes.INSTANCES_PANE.button.fire();
				FileUtils.deleteDirectory(getDirectory());
				InstanceManager.load();
				ContentPanes.INSTANCES_PANE.refresh();
			} catch (IOException e) {
				OneClientLogging.logger.error(e);
			}
		}
	}
}
