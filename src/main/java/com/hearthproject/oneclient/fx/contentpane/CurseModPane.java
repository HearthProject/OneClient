package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.api.curse.CurseModInstaller;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.api.curse.data.CurseProjects;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.ModTile;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
	public Label title;
	@FXML
	public ObservableList<ModTile> tiles = FXCollections.observableArrayList();
	@FXML
	public ListView<ModTile> listMods;
	@FXML
	public Button buttonSearch, buttonBack;
	@FXML
	public TextField textSearch;
	@FXML
	public ProgressIndicator loadingIcon;

	public Label placeholder;

	private int loadPerScroll = 4;
	private int packCount;

	public final EventHandler<ScrollEvent> scroll = event -> {
		if (event.getDeltaY() < 0) {
			if (entries == null || packCount != entries.size())
				loadMods(loadPerScroll, false);
		}
	};

	private static AsyncTask<CurseProjects> mods;
	private static List<Map.Entry<String, CurseProject>> entries;

	private volatile SimpleBooleanProperty loading;
	private Instance instance;

	public CurseModPane() {
		super("gui/contentpanes/curse_mods.fxml", "Curse Mods", "", ButtonDisplay.NONE);
		loading = new SimpleBooleanProperty(false);
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
		filterSort.valueProperty().addListener(v -> loadMods(loadPerScroll, true));

		toggleSort.selectedProperty().addListener(v -> loadMods(loadPerScroll, true));

		buttonSearch.setOnAction(action -> loadMods(loadPerScroll, true));

		listMods.setFixedCellSize(162);
		listMods.setPlaceholder(placeholder = new Label("Loading..."));
		listMods.setOnScroll(scroll);
		listMods.setItems(tiles);

		title.setText("Installing Mods to " + instance.getName());
		buttonBack.setOnAction(event -> InstancePane.show(instance));

		mods = new AsyncTask<>(Curse::getMods);
		service.submit(mods);
		mods.addListener(this::init, service);

		loadingIcon.visibleProperty().bind(loading);
		buttonSearch.disableProperty().bind(loading);
		textSearch.disableProperty().bind(loading);
		filterSort.disableProperty().bind(loading);
		toggleSort.disableProperty().bind(loading);
	}

	@Override
	protected void onStart() { }

	@Override
	public void refresh() {
	}

	public void init() {
		loadMods(loadPerScroll, false);
	}

	public void loadMods(int count, boolean reset) {
		new Thread(() -> {
			if (loading.get()) {
				return;
			}
			loading.setValue(true);
			MiscUtil.runLaterIfNeeded(() -> placeholder.setText("Loading..."));
			if (entries == null || reset) {
				MiscUtil.runLaterIfNeeded(tiles::clear);
				try {
					OneClientLogging.info("Loading Entries");
					entries = mods.get().filter(toggleSort.isSelected(), filterSort.getValue().toLowerCase(), instance.getGameVersion(), textSearch.getText());
					packCount = entries.size();
				} catch (InterruptedException | ExecutionException e) {
					OneClientLogging.error(e);
				}
			}

			if (entries == null || entries.isEmpty()) {
				loading.setValue(false);
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Mods Found"));
				return;
			}
			OneClientLogging.info("Entries Found");
			List<ModTile> mods = Lists.newArrayList();
			for (int i = 0; i < count; i++) {
				if (entries == null || entries.isEmpty())
					break;
				Map.Entry<String, CurseProject> entry = entries.remove(0);
				MiscUtil.runLaterIfNeeded(() -> mods.add(new ModTile(instance, new CurseModInstaller(entry.getValue()))));
			}
			MiscUtil.runLaterIfNeeded(() -> {
				if (reset)
					tiles.setAll(mods);
				else
					tiles.addAll(mods);
				loading.setValue(false);
			});
			OneClientLogging.info("Loaded {} of {} Mods", packCount - entries.size(), packCount);
			if (tiles.isEmpty()) {
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Mods Found"));
			}
		}).start();

	}

	public void close() {
		tiles.clear();
		listMods.getItems().clear();
	}
}
