package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.DownloadTile;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class DownloadsPane extends ContentPane {

	@FXML
	public ListView<DownloadTask> downloads;

	public DownloadsPane() {
		super("gui/contentpanes/downloads.fxml", "Downloads", "download.png", ButtonDisplay.ABOVE_DIVIDER);
	}

	@Override
	public void onStart() {
		downloads.setItems(FXCollections.observableArrayList(DownloadManager.DOWNLOADS.values()));
		downloads.setCellFactory(list -> {
			ListCell<DownloadTask> cell = new ListCell<>();
			cell.itemProperty().addListener((obs, oldItem, newItem) -> {
				if (newItem != null) {
					// update cellRoot (or its child nodes' properties) accordingly
					cell.setGraphic(new DownloadTile(newItem.getName()));
				}
			});
			cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
				if (isEmpty) {
					cell.setGraphic(null);
				}
			});
			cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			return cell;
		});
	}

	@Override
	public void refresh() {

	}
}
