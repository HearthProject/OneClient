package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.ProjectTile;
import com.hearthproject.oneclient.util.AsyncService;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public abstract class ProjectPane extends ContentPane {
    public static final ObservableList<String> VERSIONS = MinecraftUtil.getVersions(false);

    static {
        VERSIONS.add(0, "All");
    }

    public JFXToggleButton toggleSort;
    public ComboBox<String> filterSort;
    public ComboBox<String> filterVersion;
    public Label placeholder;
    public ObservableList<Database.Project> projects = FXCollections.observableArrayList();
    public ListView<ProjectTile> tiles;
    public Button buttonSearch;
    public TextField textSearch;
    public AnchorPane anchorPane;

    public ImageView loadingIcon;

    private String type;

    public ProjectPane(String name, String image, String type) {
        super("gui/contentpanes/curse_packs.fxml", name, image, ButtonDisplay.TOP);
        this.type = type;
    }

    private final EventHandler<ScrollEvent> scroll = event -> {
        if (event.getDeltaY() < 0) {
            loadPage();
        }
    };

    private AsyncService<List<Database.Project>> projectService;
    private PageService pageService;

    @Override
    protected void onStart() {
        anchorPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        anchorPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());

        loadingIcon.setImage(ImageUtil.openCachedImage(FileUtil.getResourceStream("images/loading.gif"), "loading"));
        loadingIcon.setFitHeight(32);
        loadingIcon.setFitWidth(32);

        filterVersion.setItems(VERSIONS);
        filterVersion.getSelectionModel().selectFirst();
        filterVersion.valueProperty().addListener(v -> load());

        filterSort.setItems(FXCollections.observableArrayList("Popularity", "Alphabetical", "Fuzzy"));
        filterSort.getSelectionModel().selectFirst();
        filterSort.valueProperty().addListener(v -> load());

        toggleSort.selectedProperty().addListener(v -> load());
        buttonSearch.setOnAction(action -> load());

        tiles.setFixedCellSize(162);
        tiles.setPlaceholder(placeholder = new Label("Loading..."));
        tiles.setOnScroll(scroll);

        pageService = new PageService(10, () -> projectService.getValue());
        load();
        bind();
        loadingIcon.visibleProperty().bind(pageService.runningProperty());
        buttonSearch.disableProperty().bind(pageService.runningProperty());
        textSearch.disableProperty().bind(pageService.runningProperty());
        filterSort.disableProperty().bind(pageService.runningProperty());
        filterVersion.disableProperty().bind(pageService.runningProperty());
        toggleSort.disableProperty().bind(pageService.runningProperty());
    }


    public ObservableList<Database.Project> search(String query, String version, String sort, boolean reverse) {
        return FXCollections.observableArrayList(Curse.getDatabase().searchProjects(query, type, version, sort, reverse));
    }

    public void load() {
        projectService = new AsyncService<>(() -> search(textSearch.getText(), filterVersion.getValue(), filterSort.getValue(), toggleSort.isSelected()));
        projects.clear();
        projectService.setOnSucceeded(event -> loadPage());
        projectService.start();
    }

    public void loadPage() {
        if (!pageService.isRunning()) {
            pageService.reset();
            pageService.start();
            pageService.setOnSucceeded(event -> projects.addAll(pageService.getValue()));
        }
    }

    public abstract void bind();

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
