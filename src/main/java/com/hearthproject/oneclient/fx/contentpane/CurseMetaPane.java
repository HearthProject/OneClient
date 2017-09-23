package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.api.curse.CurseImporter;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.api.curse.data.CurseProjects;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.ModpackTile;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

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
	public ObservableList<ModpackTile> tiles = FXCollections.observableArrayList();
	public ListView<ModpackTile> listPacks;
	public Button buttonSearch;
	public TextField textSearch;
	public AnchorPane anchorPane;

	public ImageView loadingIcon;

	SimpleIntegerProperty elementCount = new SimpleIntegerProperty(0);

	private PageService pageService;

	public CurseMetaPane() {
		super("gui/contentpanes/curse_packs.fxml", "Get Modpacks", "modpacks.png", ButtonDisplay.TOP);

	}

	private final EventHandler<ScrollEvent> scroll = event -> {
		if (event.getDeltaY() < 0) {
			loadPacks(false);
		}
	};

	private static AsyncTask<CurseProjects> packs;
	private static List<Map.Entry<String, CurseProject>> entries;

	@Override
	protected void onStart() {

		anchorPane.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		anchorPane.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());

		loadingIcon.setImage(ImageUtil.openCachedImage(FileUtil.getResourceStream("images/loading.gif"), "loading"));
		loadingIcon.setFitHeight(32);
		loadingIcon.setFitWidth(32);

		filterVersion.setItems(VERSIONS);
		filterVersion.getSelectionModel().selectFirst();
		filterVersion.valueProperty().addListener(v -> loadPacks(true));

		filterSort.setItems(FXCollections.observableArrayList("Popularity", "Alphabetical"));
		filterSort.getSelectionModel().selectFirst();
		filterSort.valueProperty().addListener(v -> loadPacks(true));

		toggleSort.selectedProperty().addListener(v -> loadPacks(true));

		buttonSearch.setOnAction(action -> loadPacks(true));

		listPacks.setFixedCellSize(162);
		listPacks.setPlaceholder(placeholder = new Label("Loading..."));
		listPacks.setOnScroll(scroll);
		listPacks.setItems(tiles);

		pageService = new PackService(() -> entries, tiles, placeholder.textProperty());

		packs = new AsyncTask<>(Curse::getModpacks);
		service.submit(packs);
		packs.addListener(this::init, service);

		loadingIcon.visibleProperty().bind(pageService.runningProperty());
		buttonSearch.disableProperty().bind(pageService.runningProperty());
		textSearch.disableProperty().bind(pageService.runningProperty());
		filterSort.disableProperty().bind(pageService.runningProperty());
		filterVersion.disableProperty().bind(pageService.runningProperty());
		toggleSort.disableProperty().bind(pageService.runningProperty());

	}

	public void init() {
		MiscUtil.runLaterIfNeeded(() -> loadPacks(false));
	}

	public void loadPacks(boolean reset) {
		if (!pageService.isRunning()) {
			if (entries == null || reset) {
				tiles.clear();
				try {
					entries = packs.get().filter(toggleSort.isSelected(), filterSort.getValue().toLowerCase(), filterVersion.getValue(), textSearch.getText());
				} catch (InterruptedException | ExecutionException e) {
					OneClientLogging.error(e);
				}
			}
			if (entries == null || entries.isEmpty()) {
				placeholder.setText("No Packs Found");
				return;
			}
			placeholder.setText("Loading...");
			pageService.reset();
			pageService.start();
		}
	}

	@Override
	public void refresh() {

	}

	@Override
	public void close() {
		pageService.cancel();
		listPacks.getItems().clear();
		tiles.clear();
	}

	@Override
	public boolean showInSideBar() {
		return false;
	}

	public class PackService extends PageService<ModpackTile> {

		public PackService(Supplier<List<Map.Entry<String, CurseProject>>> entries, ObservableList<ModpackTile> tiles, StringProperty placeholder) {
			super(entries, tiles, placeholder);
		}

		@Override
		protected Task<Void> createTask() {
			return new PageTask<ModpackTile>(entries.get(), tiles, placeholder) {
				@Override
				public void addElement(List<ModpackTile> elements, Map.Entry<String, CurseProject> entry) {
					Instance instance = new CurseImporter(entry.getKey()).create();
					if (instance != null) {
						MiscUtil.runLaterIfNeeded(() -> elements.add(new ModpackTile(instance)));
					}
				}
			};
		}
	}
}
