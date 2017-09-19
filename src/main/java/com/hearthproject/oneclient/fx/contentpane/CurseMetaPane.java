package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.api.curse.CurseImporter;
import com.hearthproject.oneclient.api.curse.data.CurseModpacks;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.InstallTile;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.Map;
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
	public Label placeholder;
	public ObservableList<InstallTile> tiles = FXCollections.observableArrayList();
	public ListView<InstallTile> listPacks;
	public Button buttonSearch;
	public TextField textSearch;
	public AnchorPane anchorPane;

	public ImageView loadingIcon;

	private int loadPerScroll = 10;
	private int packCount;

	public CurseMetaPane() {
		super("gui/contentpanes/curse_packs.fxml", "Get Modpacks", "modpacks.png", ButtonDisplay.TOP);

	}

	public final EventHandler<ScrollEvent> scroll = event -> {
		if (event.getDeltaY() < 0) {
			loadPacks(loadPerScroll, false);
		}
	};

	private static AsyncTask<CurseModpacks> packs;

	private static List<Map.Entry<String, CurseModpacks.CurseModpack>> entries;

	private volatile SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@Override
	protected void onStart() {
		anchorPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		anchorPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());

		loadingIcon.setImage(ImageUtil.openCachedImage(FileUtil.getResource("images/loading.gif"), "loading"));
		loadingIcon.setFitHeight(32);
		loadingIcon.setFitWidth(32);
		loadingIcon.visibleProperty().bind(loading);

		filterVersion.setItems(VERSIONS);
		filterVersion.getSelectionModel().selectFirst();
		filterVersion.valueProperty().addListener(v -> loadPacks(loadPerScroll, true));

		filterSort.setItems(FXCollections.observableArrayList("Popularity", "Alphabetical"));
		filterSort.getSelectionModel().selectFirst();
		filterSort.valueProperty().addListener(v -> loadPacks(loadPerScroll, true));

		toggleSort.selectedProperty().addListener(v -> loadPacks(loadPerScroll, true));

		buttonSearch.setOnAction(action -> loadPacks(loadPerScroll, true));

		listPacks.setPlaceholder(placeholder = new Label("Loading..."));
		listPacks.setOnScroll(scroll);
		listPacks.setItems(tiles);

		packs = new AsyncTask<>(Curse::getModpacks);
		service.submit(packs);
		packs.addListener(this::init, service);

	}

	public void init() {
		loadPacks(loadPerScroll, false);
	}

	public void loadPacks(int count, boolean reset) {
		new Thread(() -> {
			if (loading.get()) {
				return;
			}
			loading.setValue(true);
			MiscUtil.runLaterIfNeeded(() -> placeholder.setText("Loading..."));
			if (entries == null || reset) {
				MiscUtil.runLaterIfNeeded(tiles::clear);
				try {
					entries = packs.get().filter(toggleSort.isSelected(), filterSort.getValue().toLowerCase(), filterVersion.getValue(), textSearch.getText());
					packCount = entries.size();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			if (entries == null || entries.isEmpty()) {
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Packs Found"));
				return;
			}

			List<InstallTile> instances = Lists.newArrayList();
			for (int i = 0; i < count; i++) {
				if (entries == null || entries.isEmpty())
					break;
				Map.Entry<String, CurseModpacks.CurseModpack> entry = entries.remove(0);
				Instance instance = new CurseImporter(entry.getKey()).create();
				if (instance != null) {
					MiscUtil.runLaterIfNeeded(() -> instances.add(new InstallTile(instance)));
				}
			}

			MiscUtil.runLaterIfNeeded(() -> {
				if (reset)
					tiles.setAll(instances);
				else
					tiles.addAll(instances);
			});
			OneClientLogging.info("Loaded {} of {} Modpacks", packCount - entries.size(), packCount);
			loading.setValue(false);
			if (tiles.isEmpty()) {
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Packs Found"));
			}
		}).start();

	}

	@Override
	public void refresh() {

	}

	@Override
	public void close() {
		tiles.clear();
	}

	@Override
	public boolean showInSideBar() {
		return false;
	}
}
