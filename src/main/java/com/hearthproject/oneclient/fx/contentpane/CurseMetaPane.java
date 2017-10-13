package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.api.modpack.curse.CurseImporter;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.ModpackTile;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.BindUtil;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CurseMetaPane extends ContentPane {
    private static final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
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
    private SimpleBooleanProperty pageService = new SimpleBooleanProperty(false);

    public CurseMetaPane() {
        super("gui/contentpanes/curse_packs.fxml", "Get Modpacks", "modpacks.png", ButtonDisplay.TOP);
    }

    private final EventHandler<ScrollEvent> scroll = event -> {
        if (event.getDeltaY() < 0) {
            page();
        }
    };

    public AsyncTask<List<Database.Project>> modpacks;

    @Override
    protected void onStart() {

        modpacks = new AsyncTask<>(() -> FXCollections.observableArrayList(Curse.DATABASE.getPopular("modpack")));
        service.submit(modpacks);
        BindUtil.bindMapping(tiles, listPacks.getItems(), project -> {
            Instance instance = new CurseImporter(project).create();
            return new ModpackTile(instance);
        });
        modpacks.addListener(this::page, service);


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

        loadingIcon.visibleProperty().bind(pageService);
        buttonSearch.disableProperty().bind(pageService);
        textSearch.disableProperty().bind(pageService);
        filterSort.disableProperty().bind(pageService);
        filterVersion.disableProperty().bind(pageService);
        toggleSort.disableProperty().bind(pageService);


    }

    public void page() {

        try {
            List<Database.Project> add = Lists.newArrayList();
            for (int i = 0; i < 10; i++) {
                add.add(modpacks.get().remove(0));
            }
            tiles.addAll(add);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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

//	public class PackService extends PageService<ModpackTile> {
//
//		public PackService(Supplier<List<Map.Entry<String, CurseProject>>> entries, ObservableList<ModpackTile> tiles, StringProperty placeholder, IntegerProperty count) {
//			super(entries, tiles, placeholder, count);
//		}
//
//		@Override
//		protected Task<Void> createTask() {
//			return new PageTask<ModpackTile>(entries.get(), tiles, placeholder, count) {
//				@Override
//				public void addElement(List<ModpackTile> elements, Map.Entry<String, CurseProject> entry) {
//					Instance instance = new CurseImporter(entry.getKey()).create();
//					if (instance != null) {
//						MiscUtil.runLaterIfNeeded(() -> {
//							elements.add(new ModpackTile(instance));
//							page.setText((count.intValue() - entries.get().size()) + "/" + count.intValue());
//						});
//					}
//
//				}
//			};
//		}
//	}
}
