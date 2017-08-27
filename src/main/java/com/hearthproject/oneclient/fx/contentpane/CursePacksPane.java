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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

	public ListView<CurseTile> listTiles;
	public ComboBox<String> filterVersion;
	public Button buttonSearch;
	public TextField textSearch;
	private Label placeHolderMissing = new Label("No Packs Found"), placeHolderLoading = new Label("Loading Packs");
	public CursePacksPane() {
		super("gui/contentpanes/getCurseContent.fxml", "Curse Modpacks", "#2D4BAD");
	}

	private int page = 1, lastPage = -1;
	private ViewType type = ViewType.FILTER;

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
			loadPacks(page, filterVersion.getValue());
			listTiles.setOnScroll(event -> {
				if (pageDelay > 0)
					return;
				if (type == ViewType.FILTER && event.getDeltaY() < 0 && page != lastPage) {
					int old = Math.max(listTiles.getItems().size() - 8, 0);
					page++;
					loadPacks(page, filterVersion.getValue());
					listTiles.scrollTo(old);
					pageDelay = 3;
				}
			});
		}
		filterVersion.valueProperty().addListener((observableValue, s, t1) -> {
			type = ViewType.FILTER;
			tiles.clear();
			page = 1;
			loadPacks(page, filterVersion.getValue());
		});
		buttonSearch.setOnAction(event -> {
			type = ViewType.SEARCH;
			tiles.clear();
			search();
		});

	}

	public void loadPacks(int page, String version) {
		new Thread(() -> {
			try {
				if (pageLoading.get())
					return;
				pageLoading.set(true);
				List<CursePack> packs = CurseUtils.getPacks(page, version);
				if (packs != null) {
					if (!packs.isEmpty()) {
						OneClientLogging.log("Loading page " + page);
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
				OneClientLogging.log(e);
			}
		}).start();
	}

	public void search() {
		tiles.addAll(CurseUtils.searchCurse(textSearch.getText()).stream().map(p -> new CurseTile(this, p)).collect(Collectors.toList()));
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
				OneClientLogging.log(throwable);
			}

			Platform.runLater(() -> {
				InstanceManager.addInstance(instance);
				if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
					Main.mainController.currentContent.refresh();
				}
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Pack has been installed!");
				alert.setHeaderText(null);
				alert.setContentText(instance.name + " has been downloaded and installed! You can find it in the pack section.");
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
