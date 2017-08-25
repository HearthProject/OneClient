package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.fx.nodes.CurseTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.curse.CursePack;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class CursePacksPane extends ContentPane {

    public ListView<CurseTile> listTiles;
    public ObservableList<CurseTile> tiles = FXCollections.observableArrayList();

    public CursePacksPane() {
        super("gui/contentpanes/getCurseContent.fxml", "Curse Modpacks", "#2D4BAD");
    }

    private int page = 1, lastPage = -1, pageDelay = 100;
    private ViewType type = ViewType.FILTER;


    @Override
    protected void onStart() {
        listTiles.setItems(tiles);
        AnchorPane box = (AnchorPane) getNode();
        VBox.setVgrow(box, Priority.ALWAYS);
        HBox.setHgrow(listTiles, Priority.ALWAYS);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        listTiles.prefWidthProperty().bind(box.widthProperty());
        listTiles.prefHeightProperty().bind(box.heightProperty());

        if(type == ViewType.FILTER) {
            loadPacks(page);

            listTiles.setOnScroll(event -> {
                if (type == ViewType.FILTER && event.getDeltaY() < 0 && page != lastPage) {
                    int old = Math.max(listTiles.getItems().size() - 8, 0);
                    page++;
                    loadPacks(page);
                    listTiles.scrollTo(old);
                }
            });
        }

//        tiles.addAll(CurseUtils.searchCurse("All The Mods").stream().map( p -> new CurseTile(this,p)).collect(Collectors.toList()));
    }


    public void loadPacks(int page) {

        List<CursePack> packs = CurseUtils.getPacks(page);
        if (packs == null) {
            lastPage = page;
            return;
        }
        OneClientLogging.log("Loading page " + page);
        while (!packs.isEmpty()) {
            CursePack pack = packs.remove(0);
            tiles.add(new CurseTile(this, pack));
        }
    }

    public void install(MiscUtil.ThrowingConsumer<Instance> downloadFunction) {
        try {
            InstallingController.showInstaller();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InstallingController.controller.setTitleText("Installing...");
        InstallingController.controller.setDetailText("Preparing to install");

        new Thread(() -> {
            Instance instance = new Instance("Unknown");
            instance.icon = "icon.png";
            try {
                downloadFunction.accept(instance);
                MinecraftUtil.installMinecraft(instance);
            } catch (Throwable throwable) {
                OneClientLogging.log(throwable);
            }

            Platform.runLater(() -> {
                InstanceManager.addInstance(instance);
                if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
                    Main.mainController.currentContent.refresh();
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pack has been installed!");
                alert.setHeaderText(null);
                alert.setContentText(instance.name + " has been downloaded and installed! You can find it in the pack section.");
                alert.showAndWait();
            });

        }).start();
    }

    @Override
    public void refresh() {

    }


    public enum ViewType {
        FILTER,
        SEARCH
    }
}
