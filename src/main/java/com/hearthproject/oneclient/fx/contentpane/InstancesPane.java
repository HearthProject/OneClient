package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstancesPane extends ContentPane {
	public TilePane instancePane;
	public ScrollPane scrollPane;

	public ArrayList<InstanceTile> instanceTiles = new ArrayList<>();
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
		refreshInstances();
	}

	@SuppressWarnings("unused")
	public void onNewInstancePress() {
		NewInstanceController.start(null);
	}

	private void refreshInstances() {
		new Thread(() -> {
			InstanceManager.load();
			List<InstanceTile> panes = Lists.newArrayList();
			for (Instance instance : InstanceManager.getInstances()) {
				Platform.runLater(() -> panes.add(new InstanceTile(instance)));
			}
			panes.sort(Comparator.comparing(i -> i.instance.getName()));
			instanceTiles.addAll(panes);
			Platform.runLater(() -> instancePane.getChildren().setAll(panes));
		}).start();
	}

}
