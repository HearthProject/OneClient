package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.InstanceConfigController;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

public class InstancesPane extends ContentPane {
	public TilePane instancePane;
	public ScrollPane scrollPane;
	private StackPane newInstanceTile;

	public InstancesPane() {
		super("gui/contentpanes/instances.fxml", "Home", "home.png", ButtonDisplay.TOP);
	}

	@Override
	public void onStart() {
		if (newInstanceTile == null) {
			newInstanceTile = (StackPane) instancePane.getChildren().get(0);
		}
		scrollPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		scrollPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		scrollPane.setContent(instancePane);
		instancePane.prefWidthProperty().bind(scrollPane.widthProperty());
	}

	@Override
	public void refresh() {
	}

	@SuppressWarnings("unused")
	public void onNewInstancePress() {
		InstanceConfigController.start(null);
	}

}
