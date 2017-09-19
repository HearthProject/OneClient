package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.FeaturedTile;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.util.MiscUtil;
import com.jfoenix.controls.JFXButton;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

public class HomePane extends ContentPane {

	@FXML
	public StackPane root;

	@FXML
	public ListView<Instance> recent, featured;

	@FXML
	public JFXButton viewInstances, viewModpacks;

	public HomePane() {
		super("gui/contentpanes/home.fxml", "Home", "home.png", ButtonDisplay.TOP);
	}

	@Override
	protected void onStart() {
		root.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		root.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		recent.setPlaceholder(new Label("Loading Recent Instances..."));
		recent.setCellFactory(param -> {
			ListCell<Instance> cell = new ListCell<>();
			cell.itemProperty().addListener((obs, oldItem, newItem) -> {
				if (newItem != null) {
					cell.setGraphic(new InstanceTile(newItem));
				}
			});
			cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
				if (isEmpty) {
					cell.setGraphic(null);
				}
			});
			cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			return cell;
		});

		featured.setPlaceholder(new Label("Loading Featured Modpacks..."));
		featured.setCellFactory(param -> {
			ListCell<Instance> cell = new ListCell<>();
			cell.itemProperty().addListener((obs, oldItem, newItem) -> {
				if (newItem != null) {
					cell.setGraphic(new FeaturedTile(newItem));
				}
			});
			cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
				if (isEmpty) {
					cell.setGraphic(null);
				}
			});
			cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			return cell;
		});
		viewInstances.setOnAction(event -> Main.mainController.setContent(ContentPanes.INSTANCES_PANE));
		viewModpacks.setOnAction(event -> Main.mainController.setContent(ContentPanes.CURSE_META_PANE));

		new Thread(() -> {
			ObservableList<Instance> instances = InstanceManager.getRecentInstances();
			MiscUtil.runLaterIfNeeded(() -> recent.setItems(instances));
		}).start();

		new Thread(() -> {
			ObservableList<Instance> instances = InstanceManager.getFeaturedInstances();
			MiscUtil.runLaterIfNeeded(() -> featured.setItems(instances));
		}).start();
	}

	@Override
	public void refresh() {
	}

}
