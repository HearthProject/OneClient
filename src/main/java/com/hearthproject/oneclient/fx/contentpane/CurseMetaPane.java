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
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CurseMetaPane extends ContentPane {
	private static final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
	public static final ObservableList<String> VERSIONS = MinecraftUtil.getVersions(false);
	public ComboBox<String> filterSort;
	public ComboBox<String> filterVersion;
	public Label placeholder;
	public ObservableList<InstallTile> tiles = FXCollections.observableArrayList();
	public ListView<InstallTile> listPacks;
	public Button buttonSearch;
	public TextField textSearch;
	public AnchorPane anchorPane;

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

		VERSIONS.add(0, "All");
		filterVersion.setItems(VERSIONS);
		filterVersion.getSelectionModel().selectFirst();
		filterVersion.valueProperty().addListener(v -> loadPacks(loadPerScroll, true));

		filterSort.setItems(FXCollections.observableArrayList("Alphabetical"));
		filterSort.getSelectionModel().selectFirst();
		filterSort.valueProperty().addListener(v -> loadPacks(loadPerScroll, true));

		buttonSearch.setOnAction(action -> loadPacks(loadPerScroll, true));

		listPacks.setPlaceholder(placeholder = new Label("Loading..."));
		listPacks.setOnScroll(scroll);
		listPacks.setItems(tiles);

		NotifyUtil.loadingIcon().visibleProperty().bind(loading);
		packs = new AsyncTask<>(Curse::getModpacks);
		service.submit(packs);
		packs.addListener(this::init, service);

	}

	public void init() {
		loadPacks(loadPerScroll, false);
	}

	public void loadPacks(int count, boolean reset) {
		new Thread(() -> {
			MiscUtil.runLaterIfNeeded(() -> placeholder.setText("Loading..."));
			if (entries == null || reset) {
				try {
					MiscUtil.runLaterIfNeeded(tiles::clear);
					entries = packs.get().filter(filterVersion.getValue(), textSearch.getText());
					packCount = entries.size();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			if (entries.isEmpty()) {
				OneClientLogging.info("Empty :(");
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Packs Found"));
				return;
			}
			if (loading.get()) {
				OneClientLogging.info("already loading");
				return;
			}
			loading.setValue(true);
			OneClientLogging.info("Loading more");
			List<Instance> instances = Lists.newArrayList();
			for (int i = 0; i < count; i++) {
				if (entries.isEmpty())
					break;
				Map.Entry<String, CurseModpacks.CurseModpack> entry = entries.remove(0);
				Instance instance = new CurseImporter(entry.getKey()).create();
				if (instance != null)
					instances.add(instance);
			}
			List<InstallTile> newTiles = instances.stream().map(InstallTile::new).collect(Collectors.toList());
			MiscUtil.runLaterIfNeeded(() -> {
				if (reset)
					tiles.setAll(newTiles);
				else
					tiles.addAll(newTiles);
			});
			NotifyUtil.setText(Duration.seconds(5d), "Loaded %s of %s Modpacks", packCount - entries.size(), packCount);
			loading.setValue(false);
			if (tiles.isEmpty()) {
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Packs Found"));
			}
		}).start();

	}

	private Comparator<InstallTile> getSorting() {
		switch (filterSort.getValue().toLowerCase()) {
			case "popularity":
				return Comparator.comparingDouble(InstallTile::getPopularity).reversed();
			case "alphabetical":
				return Comparator.naturalOrder();
		}
		return null;
	}

	@Override
	public void refresh() {
		tiles.clear();
		NotifyUtil.clear();
		entries = null;
	}

}
