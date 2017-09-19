package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.FeaturedTile;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

public class HomePane extends ContentPane {

	@FXML
	public StackPane root;

	@FXML
	public GridView<Instance> recent;
	@FXML
	public GridView<Instance> featured;

	@FXML
	public JFXButton viewInstances, viewModpacks;

	public HomePane() {
		super("gui/contentpanes/home.fxml", "Home", "home.png", ButtonDisplay.TOP);
	}

	@Override
	protected void onStart() {
		root.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		root.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());

		recent.setCellHeight(192);
		recent.setCellWidth(192);
		recent.setHorizontalCellSpacing(6);
		recent.setVerticalCellSpacing(6);

		recent.setCellFactory(param -> {
			GridCell<Instance> cell = new GridCell<>();
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
		recent.setItems(InstanceManager.getRecentInstances());

		featured.setCellHeight(192);
		featured.setCellWidth(192);
		featured.setHorizontalCellSpacing(6);
		featured.setVerticalCellSpacing(6);

		featured.setCellFactory(param -> {
			GridCell<Instance> cell = new GridCell<>();
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
		featured.setItems(InstanceManager.getFeaturedInstances());

		viewInstances.setOnAction(event -> Main.mainController.setContent(ContentPanes.INSTANCES_PANE));
		viewModpacks.setOnAction(event -> Main.mainController.setContent(ContentPanes.CURSE_META_PANE));
	}

	@Override
	public void refresh() {
	}

}
