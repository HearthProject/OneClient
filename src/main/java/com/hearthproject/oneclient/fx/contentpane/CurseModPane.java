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
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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

	public Label placeholder;
	@FXML
	public ObservableList<ModTile> tiles = FXCollections.observableArrayList();
	@FXML
	public ListView<ModTile> listMods;
	@FXML
	public Button buttonSearch;
	@FXML
	public TextField textSearch;
	@FXML
	public ImageView loadingIcon;

	private int loadPerScroll = 10;
	private int packCount;

	public final EventHandler<ScrollEvent> scroll = event -> {
		if (event.getDeltaY() < 0) {
			if (packCount != entries.size())
				loadPacks(loadPerScroll, false);
		}
	};

	private static AsyncTask<CurseProjects> mods;
	private static List<Map.Entry<String, CurseProject>> entries;

	private volatile SimpleBooleanProperty loading = new SimpleBooleanProperty(false);
	private Instance instance;

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
			Main.mainController.currentContent.button.setSelected(false);
			Main.mainController.setContent(pane);
		}
	}

	public void setupPane(Instance instance) {
		this.instance = instance;
		loadingIcon.setImage(ImageUtil.openCachedImage(FileUtil.getResource("images/loading.gif"), "loading"));
		loadingIcon.setFitHeight(32);
		loadingIcon.setFitWidth(32);
		loadingIcon.visibleProperty().bind(loading);

		filterSort.setItems(FXCollections.observableArrayList("Popularity", "Alphabetical"));
		filterSort.getSelectionModel().selectFirst();
		filterSort.valueProperty().addListener(v -> loadPacks(loadPerScroll, true));

		toggleSort.selectedProperty().addListener(v -> loadPacks(loadPerScroll, true));

		buttonSearch.setOnAction(action -> loadPacks(loadPerScroll, true));

		listMods.setFixedCellSize(162);
		listMods.setPlaceholder(placeholder = new Label("Loading..."));
		listMods.setOnScroll(scroll);
		listMods.setItems(tiles);

		mods = new AsyncTask<>(Curse::getMods);
		service.submit(mods);
		mods.addListener(this::init, service);

		buttonSearch.disableProperty().bind(loading);
		textSearch.disableProperty().bind(loading);
		filterSort.disableProperty().bind(loading);
		toggleSort.disableProperty().bind(loading);

		title.setText("Installing Mods to " + instance.getName());
	}

	@Override
	protected void onStart() { }

	@Override
	public void refresh() {
	}

	public void init() {
		loadPacks(loadPerScroll, false);
	}

	public void loadPacks(int count, boolean reset) {
		new Thread(() -> {
			if (loading.get()) {
				return;
			}
			OneClientLogging.info("Mods: {} {} {} ", filterSort.getValue(), instance.getGameVersion(), textSearch.getText());
			loading.setValue(true);
			MiscUtil.runLaterIfNeeded(() -> placeholder.setText("Loading..."));
			if (entries == null || reset) {
				MiscUtil.runLaterIfNeeded(tiles::clear);
				try {
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
			});
			OneClientLogging.info("Loaded {} of {} Mods", packCount - entries.size(), packCount);
			loading.setValue(false);
			if (tiles.isEmpty()) {
				loading.setValue(false);
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Mods Found"));
			}
		}).start();

	}

	public void close() {
		tiles.clear();
	}
}
