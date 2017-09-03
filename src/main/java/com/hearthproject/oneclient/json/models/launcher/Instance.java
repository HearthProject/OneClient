package com.hearthproject.oneclient.json.models.launcher;

import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Instance {

	private Manifest manifest;

	public Instance() {
		this.manifest = new Manifest();
	}

	public Instance(Manifest manifest) {
		this.manifest = manifest;
	}

	public void setManifest(Manifest manifest) {
		this.manifest = manifest;
	}

	public Manifest getManifest() {
		return manifest;
	}

	public File getDirectory() {
		if (manifest != null)
			return manifest.getDirectory();
		return null;
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
				File dir = getDirectory();
				FileUtils.deleteDirectory(dir);
				InstanceManager.load();
				ContentPanes.INSTANCES_PANE.refresh();
			} catch (IOException e) {
				OneClientLogging.logger.error(e);
			}
		}
	}

	public void setName(String name) {
		int i = 0;
		manifest.name = name;
		while (!isValid()) {
			manifest.name = (name + "(" + i++ + ")");
		}
	}

	public boolean isValid() {
		return !getDirectory().exists();
	}

	public void save() {
		String manifest = JsonUtil.GSON.toJson(getManifest());
		JsonUtil.save(new File(getDirectory(), "manifest.json"), manifest);
	}

	public static Instance load(File directory) {
		Manifest manifest = JsonUtil.read(new File(directory, "manifest.json"), Manifest.class);
		if (manifest == null)
			return null;
		return new Instance(manifest);
	}
}
