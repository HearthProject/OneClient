package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.base.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.FeaturedTile;
import com.hearthproject.oneclient.fx.nodes.InstanceTile;
import com.hearthproject.oneclient.util.BindUtil;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

public class HomePane extends ContentPane {

    @FXML
    public StackPane root;

    @FXML
    public ListView<FeaturedTile> featured;
    @FXML
    public ListView<InstanceTile> recent;

    @FXML
    public JFXButton viewInstances, viewModpacks;

    public HomePane() {
        super("gui/contentpanes/home.fxml", "Home", "home.png", ButtonDisplay.TOP);
    }

    @Override
    protected void onStart() {
        root.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        root.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
        recent.setPlaceholder(new Label("No Recent Instances..."));
        featured.setPlaceholder(new Label("Loading Featured Modpacks..."));
        BindUtil.bindMapping(InstanceManager.getRecentInstances(), recent.getItems(), InstanceTile::new);
        BindUtil.bindMapping(InstanceManager.getFeaturedInstances(), featured.getItems(), instance -> new FeaturedTile(instance, instance.getInstaller()));
        viewInstances.setOnAction(event -> Main.mainController.setContent(ContentPanes.INSTANCES_PANE));
        viewModpacks.setOnAction(event -> Main.mainController.setContent(ContentPanes.CURSE_PACK_PANE));
    }

    @Override
    public void refresh() {
    }

    @Override
    public void close() {
        featured.getItems().clear();
        recent.getItems().clear();
    }
}
