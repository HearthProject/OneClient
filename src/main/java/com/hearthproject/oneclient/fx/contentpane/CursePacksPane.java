package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import javafx.scene.web.WebView;

public class CursePacksPane extends ContentPane {
	public WebView webView;

	public CursePacksPane() {
		super("gui/contentpanes/getCurseContent.fxml", "Get Curse Pack", "#DB6B20");
	}

	@Override
	protected void onStart() {
		webView.getEngine().load("https://minecraft.curseforge.com/modpacks");
		webView.minWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		webView.minHeightProperty().bind(Main.mainController.contentBox.heightProperty());
	}

	@Override
	public void refresh() {

	}
}
