package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import javafx.scene.web.WebView;

public class CursePacksPane extends ContentPane {
	public WebView webView;

	public CursePacksPane() {
		super("gui/contentpanes/getCurseContent.fxml", "Get Curse Pack", "#3A54A3");
	}

	@Override
	protected void onStart() {
		webView.getEngine().load("https://minecraft.curseforge.com/modpacks");
	}

	@Override
	public void refresh() {

	}
}
