package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;

public class DownloadsPane extends ContentPane {

	public DownloadsPane() {
		super("gui/contentpanes/downloads.fxml", "Downloads", "download.png", ButtonDisplay.NONE);
	}

	@Override
	public void onStart() {
	}

	@Override
	public void refresh() {

	}
}
