package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.api.modpack.curse.CurseImporter;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.ModpackTile;
import com.hearthproject.oneclient.util.AsyncService;
import com.hearthproject.oneclient.util.BindUtil;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class CurseMetaPane extends ContentPane {
    public static final ObservableList<String> VERSIONS = MinecraftUtil.getVersions(false);

    static {
        VERSIONS.add(0, "All");
    }

    public JFXToggleButton toggleSort;
    public ComboBox<String> filterSort;
    public ComboBox<String> filterVersion;
    public Label placeholder, page;
    public ObservableList<Database.Project> tiles = FXCollections.observableArrayList();
    public ListView<ModpackTile> listPacks;
    public Button buttonSearch;
    public TextField textSearch;
    public AnchorPane anchorPane;

    public ImageView loadingIcon;

    private SimpleIntegerProperty count = new SimpleIntegerProperty(0);
    private SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

    public CurseMetaPane() {
        super("gui/contentpanes/curse_packs.fxml", "Get Modpacks", "modpacks.png", ButtonDisplay.TOP);
    }

    private final EventHandler<ScrollEvent> scroll = event -> {
        if (event.getDeltaY() < 0) {
            load();
        }
    };

    private AsyncService<List<Database.Project>> modpacks;
    private PageService pageService;

    @Override
    protected void onStart() {
        pageService = new PageService(10, () -> modpacks.getValue());
        modpacks = new AsyncService<>(() -> FXCollections.observableArrayList(Curse.DATABASE.getPopular("modpack")));
        modpacks.start();
        BindUtil.bindMapping(tiles, listPacks.getItems(), project -> {
            Instance instance = new CurseImporter(project).create();
            return new ModpackTile(instance);
        });
        modpacks.setOnSucceeded(event -> load());
        anchorPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        anchorPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());

        loadingIcon.setImage(ImageUtil.openCachedImage(FileUtil.getResourceStream("images/loading.gif"), "loading"));
        loadingIcon.setFitHeight(32);
        loadingIcon.setFitWidth(32);

        filterVersion.setItems(VERSIONS);
        filterVersion.getSelectionModel().selectFirst();
        filterVersion.valueProperty().addListener(v -> {
        });

        filterSort.setItems(FXCollections.observableArrayList("Popularity", "Alphabetical"));
        filterSort.getSelectionModel().selectFirst();
        filterSort.valueProperty().addListener(v -> {
        });

        toggleSort.selectedProperty().addListener(v -> {
        });

        buttonSearch.setOnAction(action -> {
        });

        listPacks.setFixedCellSize(162);
        listPacks.setPlaceholder(placeholder = new Label("Loading..."));
        listPacks.setOnScroll(scroll);

        loadingIcon.visibleProperty().bind(pageService.runningProperty());
        buttonSearch.disableProperty().bind(pageService.runningProperty());
        textSearch.disableProperty().bind(pageService.runningProperty());
        filterSort.disableProperty().bind(pageService.runningProperty());
        filterVersion.disableProperty().bind(pageService.runningProperty());
        toggleSort.disableProperty().bind(pageService.runningProperty());


    }


    public void load() {
        if (!pageService.isRunning()) {
            pageService.reset();
            pageService.start();
            pageService.setOnSucceeded(event -> MiscUtil.runLaterIfNeeded(() -> tiles.addAll(pageService.getValue())));
        }
    }

    @Override
    public void refresh() {

    }

    @Override
    public void close() {
    }

    @Override
    public boolean showInSideBar() {
        return false;
    }

}
