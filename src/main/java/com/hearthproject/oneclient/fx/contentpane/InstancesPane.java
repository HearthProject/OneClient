package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.util.MiscUtil;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstancesPane extends ContentPane {
	public TilePane instancePane;
	public ScrollPane scrollPane;

	public ArrayList<InstanceTile> instanceTiles = new ArrayList<>();

	public InstancesPane() {
		super("gui/contentpanes/instances.fxml", "Home", "home.png", ButtonDisplay.TOP);
	}

	@Override
	public void onStart() {
		scrollPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		scrollPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		instancePane.prefWidthProperty().bind(scrollPane.widthProperty());

	}

	@Override
	public void refresh() {
		instancePane.getChildren().clear();
		new Thread(this::refreshInstances).start();
	}

	private void refreshInstances() {
		InstanceManager.load();
		List<Instance> instances = Lists.newArrayList(InstanceManager.getInstances());
		instances.sort(Comparator.comparing(Instance::getName));
		for (Instance instance : instances) {
			MiscUtil.runLaterIfNeeded(() -> {
				InstanceTile tile = new InstanceTile(instance);
				instancePane.getChildren().add(tile);
				instanceTiles.add(tile);
			});
		}
	}

}
