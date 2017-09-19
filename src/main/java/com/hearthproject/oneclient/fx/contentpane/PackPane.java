package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.multimc.MMCImporter;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.jfoenix.controls.JFXButton;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class PackPane extends ContentPane {

	public JFXButton buttonCustom, buttonCurse, buttonMMC;

	public PackPane() {
		super("gui/contentpanes/packs.fxml", "Get Modpacks", "modpacks.png", ButtonDisplay.TOP);
	}

	@Override
	protected void onStart() {
		buttonCustom.setOnAction(event -> NewInstanceController.start(null));

		buttonCurse.setOnAction(event -> {
			Main.mainController.currentContent.button.setSelected(false);
			Main.mainController.setContent(ContentPanes.CURSE_META_PANE);
		});

		buttonMMC.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip"));
			File file = chooser.showOpenDialog(null);
			if (file != null && FilenameUtils.isExtension(file.toString(), ".zip")) {
				Instance instance = new MMCImporter(file).create();
				if (instance != null) {
					instance.install();
				}
			}
		});
	}

	@Override
	public void refresh() {

	}
}
