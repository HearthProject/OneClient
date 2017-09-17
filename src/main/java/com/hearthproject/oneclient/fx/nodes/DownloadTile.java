package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.DownloadManager;
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
	private MenuItem cancel;

	public DownloadTile(String name) {
		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/download_tile.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(loc);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		title.setText("Downloading " + name);
		progress.progressProperty().bind(DownloadManager.progressProperty(name));
		info.textProperty().bind(DownloadManager.messageProperty(name));
		cancel.setOnAction(event -> DownloadManager.get(name).cancel(true));
	}
}
