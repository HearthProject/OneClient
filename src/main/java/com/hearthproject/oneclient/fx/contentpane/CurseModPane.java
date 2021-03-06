package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.api.modpack.curse.CurseModInstaller;
import com.hearthproject.oneclient.api.modpack.curse.data.CurseFullProject;
import com.hearthproject.oneclient.api.modpack.curse.data.CurseProject;
import com.hearthproject.oneclient.api.modpack.curse.data.CurseProjects;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.ModTile;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.util.function.Supplier;

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

	private SimpleIntegerProperty count = new SimpleIntegerProperty(0);

	public final EventHandler<ScrollEvent> scroll = event -> {
		if (event.getDeltaY() < 0) {
			loadMods(false);
		}
	};

	private static AsyncTask<CurseProjects> mods;
	private static List<Map.Entry<String, CurseProject>> entries;

	private Instance instance;
	private PageService pageService;

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

		pageService = new ModService(() -> entries, tiles, placeholder.textProperty(), instance, count);

		mods = new AsyncTask<>(Curse::getMods);
		service.submit(mods);
		mods.addListener(this::init, service);

		loadingIcon.visibleProperty().bind(pageService.runningProperty());
		buttonSearch.disableProperty().bind(pageService.runningProperty());
		textSearch.disableProperty().bind(pageService.runningProperty());
		filterSort.disableProperty().bind(pageService.runningProperty());
		toggleSort.disableProperty().bind(pageService.runningProperty());
	}

	@Override
	protected void onStart() { }

	@Override
	public void refresh() {
	}

	public void init() {
		loadMods(true);
	}

	public void loadMods(boolean reset) {

		if (!pageService.isRunning()) {
			if (entries == null || reset) {
				tiles.clear();
				try {
					OneClientLogging.info("Loading Entries");
					entries = mods.get().filter(toggleSort.isSelected(), filterSort.getValue().toLowerCase(), instance.getGameVersion(), textSearch.getText());
					count.set(entries.size());
				} catch (InterruptedException | ExecutionException e) {
					OneClientLogging.error(e);
				}
			}
			if (entries == null || entries.isEmpty()) {
				placeholder.setText("No Mods Found");
				return;
			}
			placeholder.setText("Loading...");
			pageService.reset();
			pageService.start();
		}
	}

	public void close() {
		tiles.clear();
	}

	public class ModService extends PageService<ModTile> {
		private Instance instance;

		public ModService(Supplier<List<Map.Entry<String, CurseProject>>> entries, ObservableList<ModTile> tiles, StringProperty placeholder, Instance instance, IntegerProperty count) {
			super(entries, tiles, placeholder, count);
			this.instance = instance;
		}

		@Override
		protected Task<Void> createTask() {
			return new PageTask<ModTile>(entries.get(), tiles, placeholder, count) {
				@Override
				public void addElement(List<ModTile> elements, Map.Entry<String, CurseProject> entry) {
					CurseFullProject project = JsonUtil.read(Curse.getProjectURL(entry.getValue().Id), CurseFullProject.class);
					CurseModInstaller installer = new CurseModInstaller(instance, project);
					installer.setResolveDependencies(true);
					if (instance != null) {
						MiscUtil.runLaterIfNeeded(() -> {
							elements.add(new ModTile(instance, installer));
							page.setText((count.intValue() - entries.get().size()) + "/" + count.intValue());
						});
					}
				}
			};
		}
	}
}
