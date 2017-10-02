package com.hearthproject.oneclient.hearth.fx;

import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.hearth.api.json.packs.ModPack;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NewVersionPane extends ContentPane {

	public static ModPack modPack;

	public TextField textVersion;
	public TextArea textChangelog;
	public TextField textFile;
	public Button buttonBrowse;
	public Button buttonUpload;

	public NewVersionPane() {
		super("gui/hearth/new_version.fxml", "New Version", "", ButtonDisplay.NONE);
	}

	@Override
	protected void onStart() {

	}

	@Override
	public void refresh() {

	}
}
