package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

public class InstancesPane extends ContentPane {

	public GridView<Instance> gridView;
	public StackPane root;

	public InstancesPane() {
		super("gui/contentpanes/instances.fxml", "Instances", "instances.png", ButtonDisplay.TOP);
	}

	@Override
	public void onStart() {
		root.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		root.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		gridView.setCellHeight(192);
		gridView.setCellWidth(192);
		gridView.setHorizontalCellSpacing(6);
		gridView.setVerticalCellSpacing(6);
		gridView.setCellFactory(param -> {
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

		new Thread(() -> {
			ObservableList<Instance> instances = InstanceManager.getInstances();
			MiscUtil.runLaterIfNeeded(() -> gridView.setItems(instances));
		}).start();
		ContextMenu menu = new ContextMenu();
		MenuItem open = new MenuItem("Open Instance Folder");
		menu.getItems().addAll(open);
		open.setOnAction(event -> OperatingSystem.openWithSystem(Constants.INSTANCEDIR));
		gridView.setContextMenu(menu);
	}

	@Override
	public void refresh() {
		InstanceManager.load();
	}

}
