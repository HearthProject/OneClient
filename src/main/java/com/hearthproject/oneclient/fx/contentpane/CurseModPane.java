package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.CurseModTile;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.Executors;

public class CurseModPane extends ContentPane {
    private static final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    public static final ObservableList<String> VERSIONS = MinecraftUtil.getVersions(false);

    static {
        VERSIONS.add(0, "All");
    }

    @FXML
    public JFXToggleButton toggleSort;
    @FXML
    public ComboBox<String> filterSort;
    @FXML
    public Label title, page;
    @FXML
    public ObservableList<CurseModTile> tiles = FXCollections.observableArrayList();
    @FXML
    public ListView<CurseModTile> listMods;
    @FXML
    public Button buttonSearch, buttonBack;
    @FXML
    public TextField textSearch;
    @FXML
    public ProgressIndicator loadingIcon;

    public Label placeholder;

    private SimpleIntegerProperty count = new SimpleIntegerProperty(0);

    public final EventHandler<ScrollEvent> scroll = event -> {
        if (event.getDeltaY() < 0) {
            loadMods(false);
        }
    };

    private Instance instance;
    private PageService pageService;
    private List<Database.Project> mods;

    public CurseModPane() {
        super("gui/contentpanes/curse_mods.fxml", "Curse Mods", "", ButtonDisplay.NONE);
    }

    public static void show(Instance instance) {

        CurseModPane pane = ContentPanes.getPane(CurseModPane.class);
        if (pane != null) {
            pane.setupPane(instance);
            AnchorPane node = (AnchorPane) pane.getNode();
            node.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
            node.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
            VBox.setVgrow(node, Priority.ALWAYS);
            HBox.setHgrow(node, Priority.ALWAYS);
            Main.mainController.setContent(pane);
        }
    }

    public void setupPane(Instance instance) {
        this.instance = instance;

        filterSort.setItems(FXCollections.observableArrayList("Popularity", "Alphabetical"));
        filterSort.getSelectionModel().selectFirst();
        filterSort.valueProperty().addListener(v -> loadMods(true));

        toggleSort.selectedProperty().addListener(v -> loadMods(true));

        buttonSearch.setOnAction(action -> loadMods(true));

        listMods.setFixedCellSize(162);
        listMods.setPlaceholder(placeholder = new Label("Loading..."));
        listMods.setOnScroll(scroll);
        listMods.setItems(tiles);

        title.setText("Installing Mods to " + instance.getName());
        buttonBack.setOnAction(event -> InstancePane.show(instance));

        mods = Curse.DATABASE.getPopular("mods");

        loadingIcon.visibleProperty().bind(pageService.runningProperty());
        buttonSearch.disableProperty().bind(pageService.runningProperty());
        listMods.disableProperty().bind(pageService.runningProperty());
        textSearch.disableProperty().bind(pageService.runningProperty());
        filterSort.disableProperty().bind(pageService.runningProperty());
        toggleSort.disableProperty().bind(pageService.runningProperty());
    }

    @Override
    protected void onStart() {
    }

    @Override
    public void refresh() {
    }

    public void init() {
        loadMods(true);
    }

    public void loadMods(boolean reset) {


//        if (!pageService.isRunning()) {
//
//            placeholder.setText("Loading...");
//            pageService.reset();
//            pageService.start();
//        }
    }

    public void close() {
        tiles.clear();
    }

}
