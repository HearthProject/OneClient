package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainController {
	@FXML
	public VBox buttonBox;
	@FXML
	public VBox contentBox;
	@FXML
	public VBox sideBox;
	@FXML
	public Text copyrightInfo;
	@FXML
	public Hyperlink siteLink;
	@FXML
	public Text versionText;
	@FXML
	public ImageView imageBox;
	@FXML
	public StatusBar statusBar;

	public Label labelProgress;

	public ContentPane currentContent = null;
	public ArrayList<ContentPane> contentPanes = new ArrayList<>();

	public void onStart(Stage stage) throws IOException {
		for (ContentPane pane : ContentPanes.panesList) {
			buttonBox.getChildren().add(pane.getButton());
		}
		if (Constants.getVersion() == null) {
			versionText.setText("One Client");
		} else {
			versionText.setText("One Client v" + Constants.getVersion());
		}
		String yearString = "2017";
		int year = Calendar.getInstance().get(Calendar.YEAR);
		if (year > 2017) {
			yearString += "-" + year;
		}
		copyrightInfo.setText("©" + yearString + " - HEARTH PROJECT");
		setContent(ContentPanes.INSTANCES_PANE);
		onSceneResize(stage.getScene());
		statusBar.setText("");
		labelProgress = new Label();
		statusBar.getRightItems().add(labelProgress);
	}

	public void onSceneResize(Scene scene) {
		contentBox.setPrefWidth(scene.getWidth() - sideBox.getMinWidth());
		contentBox.setPrefHeight(scene.getHeight());
	}

	public void setContent(ContentPane content) {
		if (content == null) {
			contentBox.getChildren().clear();
		} else if (content == currentContent) {
			return;
		} else {
			contentBox.getChildren().clear();
			if (content.getNode() != null) {
				contentBox.getChildren().setAll(content.getNode());
				currentContent = content;
				currentContent.start();
			} else {
				currentContent = null;
			}
		}
	}

	public void openWebsite() {
		OperatingSystem.browseURI("http://hearthproject.uk/");
	}
}
