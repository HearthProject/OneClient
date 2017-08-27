package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.fx.nodes.CurseTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.curse.CursePack;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class CursePacksPane extends ContentPane {

	public String URL;

	public ObservableList<CurseTile> tiles = FXCollections.observableArrayList();
	public ObservableList<String> versions;
	public ObservableList<Pair<String, String>> sorting;

	public ListView<CurseTile> listTiles;
	public ComboBox<String> filterVersion;
	public ComboBox<Pair<String, String>> filterSort;
	public Button buttonSearch;
	public TextField textSearch;
	private Label placeHolderMissing = new Label("No Packs Found"), placeHolderLoading = new Label("Loading Packs");

	public CursePacksPane() {
		super("gui/contentpanes/getCurseContent.fxml", "Curse Modpacks", "#2D4BAD");
	}

	private int page = 1, lastPage = -1;
	private volatile ViewType type = ViewType.FILTER;

	private static int pageDelay;
	private static Timer timer = new Timer();

	static {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (pageDelay > 0)
					pageDelay--;
			}
		}, 1000, 1000);
	}

	private static volatile BooleanProperty pageLoading = new SimpleBooleanProperty();

	@Override
	protected void onStart() {
		versions = FXCollections.observableArrayList(CurseUtils.getVersions());
		sorting = FXCollections.observableArrayList(CurseUtils.getSorting());

		filterVersion.setItems(versions);
		filterVersion.getSelectionModel().selectFirst();
		filterVersion.setConverter(new StringConverter<String>() {
			@Override
			public String toString(String s) {
				if (s.isEmpty())
					return "All";
				return s;
			}

			@Override
			public String fromString(String s) {
				if (s.equals("All"))
					return "";
				return s.replace(" ", "+");
			}
		});

		filterSort.setItems(sorting);
		filterSort.getSelectionModel().selectFirst();
		filterSort.setConverter(new StringConverter<Pair<String, String>>() {
			@Override
			public String toString(Pair<String, String> pair) {
				return pair.getKey();
			}

			@Override
			public Pair<String, String> fromString(String s) {
				return sorting.stream().filter(k -> k.getKey().equals(s)).findFirst().orElse(null);
			}
		});
		pageLoading.addListener((observableValue, oldValue, newValue) -> {
		});
		listTiles.setItems(tiles);
		AnchorPane box = (AnchorPane) getNode();
		VBox.setVgrow(box, Priority.ALWAYS);
		HBox.setHgrow(listTiles, Priority.ALWAYS);
		HBox.setHgrow(box, Priority.ALWAYS);
		box.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		listTiles.prefWidthProperty().bind(box.widthProperty());
		listTiles.prefHeightProperty().bind(box.heightProperty());

		placeHolderMissing.setTextFill(Color.web("#FFFFFF"));
		placeHolderLoading.setTextFill(Color.web("#FFFFFF"));
		listTiles.setPlaceholder(placeHolderLoading);

		if (type == ViewType.FILTER) {
			loadPacks(page, filterVersion.getValue(), filterSort.getValue().getValue());
			listTiles.setOnScroll(event -> {
				if (pageDelay > 0)
					return;
				if (type == ViewType.FILTER && event.getDeltaY() < 0 && page != lastPage) {
					int old = Math.max(listTiles.getItems().size() - 8, 0);
					page++;
					loadPacks(page, filterVersion.getValue(), filterSort.getValue().getValue());
					listTiles.scrollTo(old);
					pageDelay = 3;
				}
			});
		}
		filterVersion.valueProperty().addListener((observableValue, s, t1) -> refreshFilters());
		filterSort.valueProperty().addListener((observableValue, s, t1) -> refreshFilters());

		buttonSearch.setOnAction(event -> search());
		textSearch.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
				search();
		});
	}

	public void refreshFilters() {
		type = ViewType.FILTER;
		tiles.clear();
		page = 1;
		loadPacks(page, filterVersion.getValue(), filterSort.getValue().getValue());
	}

	public void loadPacks(int page, String version, String sorting) {
		new Thread(() -> {
			try {
				if (pageLoading.get())
					return;
				pageLoading.set(true);
				List<CursePack> packs = CurseUtils.getPacks(page, version, sorting);
				if (packs != null) {
					if (!packs.isEmpty()) {
						OneClientLogging.logger.info("Loading page " + page);
						while (!packs.isEmpty()) {
							CursePack pack = packs.remove(0);
							Platform.runLater(() -> tiles.add(new CurseTile(this, pack)));
						}
					} else {
						lastPage = page;
					}
				} else {
					Platform.runLater(() -> listTiles.setPlaceholder(placeHolderMissing));
				}
				pageLoading.set(false);
			} catch (Exception e) {
				OneClientLogging.logger.error(e);
			}
		}).start();
	}

	public void search() {
		new Thread(() -> {
			type = ViewType.SEARCH;

			List<CursePack> packs = CurseUtils.searchCurse(textSearch.getText());
			Platform.runLater(() -> {
				tiles.clear();
				tiles.addAll(packs.stream().map(p -> new CurseTile(this, p)).collect(Collectors.toList()));
			});
		}).start();
	}

	public void install(MiscUtil.ThrowingConsumer<Instance> downloadFunction) {
		try {
			InstallingController.showInstaller();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InstallingController.controller.setTitleText("Installing...");
		InstallingController.controller.setDetailText("Preparing to install");

		new Thread(() -> {
			Instance instance = new Instance("Unknown");
			instance.icon = "icon.png";
			try {
				downloadFunction.accept(instance);
				MinecraftUtil.installMinecraft(instance);
			} catch (Throwable throwable) {
				OneClientLogging.logger.error(throwable);
			}

			Platform.runLater(() -> {
				InstanceManager.addInstance(instance);
				if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
					Main.mainController.currentContent.refresh();
				}
				InstallingController.close();
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Pack has been installed!");
				alert.setHeaderText(null);
				alert.setContentText(instance.name + " has been downloaded and installed! You can find it under the instances tab.");
				alert.showAndWait();
			});

		}).start();
	}

	@Override
	public void refresh() {

	}

	public enum ViewType {
		FILTER,
		SEARCH
	}
}
