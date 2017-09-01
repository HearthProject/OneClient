package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.CurseModpack;
import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class CursePacksPane extends ContentPane {

	public String URL;

	public ObservableList<CurseModpack> tiles = FXCollections.observableArrayList();
	public ObservableList<String> versions;
	public ObservableList<Pair<String, String>> sorting;

	public ListView<CurseModpack> listTiles;
	public ComboBox<String> filterVersion;
	public ComboBox<Pair<String, String>> filterSort;
	public Button buttonSearch;
	public TextField textSearch;
	private Label placeHolderMissing = new Label("No Packs Found"), placeHolderLoading = new Label("Loading Packs");

	public CursePacksPane() {
		super("gui/contentpanes/curse_packs.fxml", "Get Modpacks", "modpacks.png");
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
		filterSort.setItems(sorting);

		filterVersion.getSelectionModel().selectFirst();
		filterSort.getSelectionModel().selectFirst();

		filterVersion.valueProperty().addListener((observableValue, s, t1) -> refreshFilters());
		filterSort.valueProperty().addListener((observableValue, s, t1) -> refreshFilters());



		filterVersion.setConverter(new StringConverter<String>() {
			@Override
			public String toString(String s) {
				if (s.isEmpty())
					return "All";
				System.out.println(s);
				return s;
			}

			@Override
			public String fromString(String s) {
				if (s.equals("All"))
					return "";
				return s.replace(" ", "+");
			}
		});

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
		filterVersion.getSelectionModel().selectFirst();
		filterSort.getSelectionModel().selectFirst();
		loadPacks(page, filterVersion.getValue(), filterSort.getValue().getValue());
	}

	public void loadPacks(int page, String version, String sorting) {
		new Thread(() -> {
			try {
				if (pageLoading.get())
					return;
				pageLoading.set(true);
				List<CurseElement> packs = CurseUtils.getPacks(page, version, sorting);
				if (packs != null) {
					if (!packs.isEmpty()) {
						OneClientLogging.logger.info("Loading page " + page);
						while (!packs.isEmpty()) {
							CurseElement pack = packs.remove(0);
							Platform.runLater(() -> tiles.add(new CurseModpack(pack)));
						}
					} else {
						lastPage = page;
					}
				} else {
					Platform.runLater(() -> listTiles.setPlaceholder(placeHolderMissing));
				}
				pageLoading.set(false);
			} catch (Exception e) {
				OneClientLogging.error(e);
			}
		}).start();
	}

	public void search() {
		new Thread(() -> {
			type = ViewType.SEARCH;

			List<CurseElement> packs = CurseUtils.searchCurse(textSearch.getText(), "modpacks");
			Platform.runLater(() -> {
				tiles.clear();
				tiles.addAll(packs.stream().map(p -> new CurseModpack(p)).collect(Collectors.toList()));
			});
		}).start();
	}


	@Override
	public void refresh() {

	}

	@Override
	public void close() {
		this.tiles.clear();
	}

	public enum ViewType {
		FILTER,
		SEARCH
	}
}
