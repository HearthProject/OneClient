package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.ContentPaneButton;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.io.IOException;
import java.util.ArrayList;

public class MainController {
	@FXML
	public VBox topButtonBox;
	@FXML
	public VBox aboveDividerButtonBox;
	@FXML
	public VBox belowDividerButtonBox;
	@FXML
	public VBox contentBox;
	@FXML
	public VBox sideBox;
	@FXML
	public ImageView imageBox;
	@FXML
	public StatusBar statusBar;

	public Label labelProgress;
	public HBox profileBox;

	public ContentPane currentContent = null;
	public ArrayList<ContentPane> contentPanes = new ArrayList<>();

	public void onStart(Stage stage) throws IOException {
		profileBox = new HBox();
		MinecraftAuthController.loadGuiElements(profileBox);
		topButtonBox.getChildren().add(profileBox);
		for (ContentPane pane : ContentPanes.panesList) {
			ContentPaneButton button = pane.getButton();
			switch (button.getButtonDisplay()) {
				case TOP:
					topButtonBox.getChildren().add(button);
					break;
				case ABOVE_DIVIDER:
					aboveDividerButtonBox.getChildren().add(button);
					break;
				case BELOW_DIVIDER:
					belowDividerButtonBox.getChildren().add(button);
					break;
			}
		}
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

	public void openCreeperHostSite() {
		OperatingSystem.browseURI("http://partners.creeper.host/r/theoneclientxae");
	}
}
