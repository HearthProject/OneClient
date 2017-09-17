package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstancesPane extends ContentPane {
	public TilePane instancePane;
	public ScrollPane scrollPane;

	public ArrayList<InstanceTile> instanceTiles = new ArrayList<>();
	private boolean newButtonVisibility = false;

	public InstancesPane() {
		super("gui/contentpanes/instances.fxml", "Home", "home.png", ButtonDisplay.TOP);
	}

	@Override
	public void onStart() {
		scrollPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		scrollPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		scrollPane.setContent(instancePane);
		instancePane.prefWidthProperty().bind(scrollPane.widthProperty());
	}

	@Override
	public void refresh() {
		refreshInstances();
	}

	private void refreshInstances() {
		new Thread(() -> {
			InstanceManager.load();
			List<InstanceTile> panes = Lists.newArrayList();
			List<Instance> instances = Lists.newArrayList(InstanceManager.getInstances());
			instances.sort(Comparator.comparing(Instance::getName));
			for (Instance instance : instances) {
				Platform.runLater(() -> panes.add(new InstanceTile(instance)));
			}
			instanceTiles.addAll(panes);
			Platform.runLater(() -> {
				instancePane.getChildren().setAll(panes);
			});
		}).start();
	}

}
