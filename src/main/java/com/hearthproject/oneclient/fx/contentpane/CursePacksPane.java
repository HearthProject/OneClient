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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CursePacksPane extends ContentPane {
  
    public String URL;

    public ObservableList<CurseTile> tiles = FXCollections.observableArrayList();
    public ObservableList<String> versions;

    public ListView<CurseTile> listTiles;
    public ComboBox<String> filterVersion;

    public CursePacksPane() {
        super("gui/contentpanes/getCurseContent.fxml", "Curse Modpacks", "#2D4BAD");
    }

    private int page = 1, lastPage = -1, pageDelay;
    private ViewType type = ViewType.FILTER;
    private Timer timer;
    private TimerTask cooldownTask = new TimerTask() {
        @Override
        public void run() {
            if (pageDelay > 0)
                pageDelay--;
        }
    };

    @Override
    protected void onStart() {
        timer = new Timer();
	    timer.purge();
	    timer.scheduleAtFixedRate(cooldownTask, 1000, 1000);
        versions = FXCollections.observableArrayList(CurseUtils.getVersions());
        System.out.println(versions);
        filterVersion.setItems(versions);
        filterVersion.getSelectionModel().selectFirst();
        filterVersion.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String s) {
                if (s.isEmpty())
                    return "All";
                return s;
            }

            @Override
            public String fromString(String s) {
                if (s.equals("All"))
                    return "";
                return s.replace(" ", "+");
            }
        });
        listTiles.setItems(tiles);
        AnchorPane box = (AnchorPane) getNode();
        VBox.setVgrow(box, Priority.ALWAYS);
        HBox.setHgrow(listTiles, Priority.ALWAYS);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        listTiles.prefWidthProperty().bind(box.widthProperty());
        listTiles.prefHeightProperty().bind(box.heightProperty());

        if (type == ViewType.FILTER) {
            filterVersion.valueProperty().addListener((observableValue, s, t1) -> {
                tiles.clear();
                page = 1;
                loadPacks(page, filterVersion.getValue());
            });
            loadPacks(page, filterVersion.getValue());
            listTiles.setOnScroll(event -> {
                if (pageDelay > 0)
                    return;
                if (type == ViewType.FILTER && event.getDeltaY() < 0 && page != lastPage) {
                    int old = Math.max(listTiles.getItems().size() - 8, 0);
                    page++;
                    loadPacks(page, filterVersion.getValue());
                    listTiles.scrollTo(old);
                    pageDelay = 5;
                }
            });
        }
        //TODO implement searching
        //        tiles.addAll(CurseUtils.searchCurse("All The Mods").stream().map( p -> new CurseTile(this,p)).collect(Collectors.toList()));
    }

    public void loadPacks(int page, String version) {

        List<CursePack> packs = CurseUtils.getPacks(page, version);
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
