package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.jfoenix.controls.JFXProgressBar;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class DownloadTile extends VBox {
	@FXML
	private JFXProgressBar progress;

	@FXML
	private Label info;

	@FXML
	private Label title;

	@FXML
	private MenuItem cancel, remove;

	private DownloadTask task;

	public DownloadTile(String name) {
		task = DownloadManager.get(name);
		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/download_tile.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(loc);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		getStylesheets().add("gui/css/theme.css");
		title.setText("Downloading " + name);
		progress.progressProperty().bind(DownloadManager.progressProperty(name));
		info.textProperty().bind(DownloadManager.messageProperty(name));
		cancel.setOnAction(event -> task.cancel(true));
		remove.setOnAction(event -> {
			task.setRemoved(true);
			ContentPanes.DOWNLOADS_PANE.downloads.refresh();
		});

	}

	public boolean isRemoved() {
		return task.isRemoved();
	}
}
