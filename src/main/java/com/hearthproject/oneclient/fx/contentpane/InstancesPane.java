package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.base.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.util.BindUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.GridView;

public class InstancesPane extends ContentPane {

    public GridView<InstanceTile> gridView;
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
        BindUtil.bindMapping(InstanceManager.getInstances(), gridView.getItems(), InstanceTile::new);
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
