package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.multimc.MMCImporter;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.jfoenix.controls.JFXButton;
import javafx.stage.FileChooser;

import java.io.File;

public class PackPane extends ContentPane {

	public JFXButton buttonCurse, buttonMMC;

	public PackPane() {
		super("gui/contentpanes/packs.fxml", "Get Modpacks", "modpacks.png", ButtonDisplay.TOP);
	}

	@Override
	protected void onStart() {
		buttonCurse.setOnAction(event -> {
			Main.mainController.currentContent.button.setSelected(false);
			Main.mainController.setContent(ContentPanes.CURSE_META_PANE);
		});

		buttonMMC.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Zip File", ".zip"));
			File file = chooser.showOpenDialog(null);
			Instance instance = new MMCImporter(file).create();
			if (instance != null) {
				instance.install();
			}
		});
	}

	@Override
	public void refresh() {

	}
}
