package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.Mod;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.api.curse.data.CurseModData;
import com.hearthproject.oneclient.api.curse.data.CurseMods;
import com.hearthproject.oneclient.fx.nodes.ModTile;
import com.hearthproject.oneclient.util.AsyncTask;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.hearthproject.oneclient.util.minecraft.MinecraftUtil.MINECRAFT_VERSIONS;

public class ModInstallPane extends AnchorPane {

	private static final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
	private static AsyncTask<CurseMods> mods;
	private static List<Map.Entry<String, CurseModData>> entries;

	public ComboBox<String> filterVersion;
	public ListView<ModTile> listTiles;

	public ObservableList<ModTile> tiles = FXCollections.observableArrayList();

	public Instance instance;
	public Button buttonSearch;
	public TextField textSearch;
	public Label placeholder;

	private int loadPerScroll = 10;
	private volatile SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	public final EventHandler<ScrollEvent> scroll = event -> {
		if (event.getDeltaY() < 0) {
			loadPacks(loadPerScroll, false);
		}
	};

	public ModInstallPane(Instance instance) {
		this.instance = instance;
		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/curse_mods.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(loc);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		Stage stage = new Stage();
		stage.setTitle("Curse Mods");
		stage.setResizable(false);
		stage.initOwner(Main.stage);
		stage.initModality(Modality.WINDOW_MODAL);
		Scene scene = new Scene(this, 600, 500);
		scene.getStylesheets().add("gui/css/theme.css");
		stage.setScene(scene);
		stage.show();
		getStylesheets().add("gui/css/theme.css");
		maxWidthProperty().bind(scene.widthProperty());
		maxHeightProperty().bind(scene.heightProperty());

		buttonSearch.setOnAction(action -> loadPacks(loadPerScroll, true));

		filterVersion.setItems(MINECRAFT_VERSIONS);
		filterVersion.getSelectionModel().selectFirst();
		filterVersion.valueProperty().addListener(v -> loadPacks(loadPerScroll, true));

		listTiles.setPlaceholder(placeholder = new Label("Loading..."));
		listTiles.setOnScroll(scroll);
		listTiles.setItems(tiles);

		placeholder = new Label();
		listTiles.setPlaceholder(placeholder);
		listTiles.setItems(tiles);
		mods = new AsyncTask<>(Curse::getMods);
		service.submit(mods);
		mods.addListener(this::init, service);
	}

	public void init() { loadPacks(loadPerScroll, false);}

	private int packCount;

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
					entries = mods.get().filter(filterVersion.getValue(), textSearch.getText());
					packCount = entries.size();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			if (entries == null || entries.isEmpty()) {
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Mods Found"));
				return;
			}

			List<Mod> mods = Lists.newArrayList();
			for (int i = 0; i < count; i++) {
				if (entries == null || entries.isEmpty())
					break;
				Map.Entry<String, CurseModData> entry = entries.remove(0);
				mods.add(entry.getValue().create());
			}
			List<ModTile> newTiles = mods.stream().map(mod -> new ModTile(instance, mod)).collect(Collectors.toList());
			MiscUtil.runLaterIfNeeded(() -> {
				if (reset)
					tiles.setAll(newTiles);
				else
					tiles.addAll(newTiles);
			});
			NotifyUtil.setText(Duration.seconds(10d), "Loaded %s of %s Mods", packCount - entries.size(), packCount);
			loading.setValue(false);
			if (tiles.isEmpty()) {
				MiscUtil.runLaterIfNeeded(() -> placeholder.setText("No Mods Found"));
			}
		}).start();

	}

}
