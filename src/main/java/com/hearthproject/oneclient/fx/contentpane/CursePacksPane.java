package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.util.Timer;
import java.util.TimerTask;

public class CursePacksPane extends ContentPane {

	public String URL;

	//	public ObservableList<CurseModpack> tiles = FXCollections.observableArrayList();
	public ObservableList<String> versions;
	public ObservableList<CurseUtils.Filter> sorting;

	//	public ListView<CurseModpack> listTiles;
	public ComboBox<String> filterVersion;
	public ComboBox<CurseUtils.Filter> filterSort;
	public Button buttonSearch;
	public TextField textSearch;

	public AnchorPane box;
	private Label placeHolderMissing = new Label("No Packs Found"), placeHolderLoading = new Label("Loading Packs");

	public CursePacksPane() {
		super("gui/contentpanes/curse_packs.fxml", "Get CurseModpacks", "modpacks.png", ButtonDisplay.TOP);
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
		versions = CurseUtils.getVersions();
		sorting = CurseUtils.getSorting();

		filterVersion.setItems(versions);
		filterSort.setItems(sorting);

		filterVersion.getSelectionModel().selectFirst();
		filterSort.getSelectionModel().selectFirst();

		filterVersion.valueProperty().addListener((observableValue, s, t1) -> refreshFilters());
		filterSort.valueProperty().addListener((observableValue, s, t1) -> refreshFilters());

		filterVersion.setConverter(new CurseUtils.VersionConverter());
		filterSort.setConverter(new CurseUtils.FilterConverter());

		//		listTiles.setItems(tiles);
		//		VBox.setVgrow(box, Priority.ALWAYS);
		//		HBox.setHgrow(listTiles, Priority.ALWAYS);
		//		HBox.setHgrow(box, Priority.ALWAYS);
		//		box.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		//		listTiles.prefWidthProperty().bind(box.widthProperty());
		//		listTiles.prefHeightProperty().bind(box.heightProperty());
		//
		//		placeHolderMissing.setTextFill(Color.web("#FFFFFF"));
		//		placeHolderLoading.setTextFill(Color.web("#FFFFFF"));
		//		listTiles.setPlaceholder(placeHolderLoading);
		//
		//		if (type == ViewType.FILTER) {
		//			loadPacks(page, getVersion(), getFilter());
		//
		//			listTiles.setOnScroll(event -> MiscUtil.runLaterIfNeeded(() -> {
		//				if (pageDelay > 0)
		//					return;
		//				if (type == ViewType.FILTER && event.getDeltaY() < 0 && page != lastPage) {
		//					int old = Math.max(listTiles.getItems().size() - 8, 0);
		//					page++;
		//					loadPacks(page, getVersion(), getFilter());
		//					listTiles.scrollTo(old);
		//					pageDelay = 3;
		//				}
		//			}));
		//		}
		//
		//		buttonSearch.setOnAction(event -> search());
		//		textSearch.setOnKeyPressed(keyEvent -> {
		//			if (keyEvent.getCode() == KeyCode.ENTER)
		//				search();
		//		});
	}

	public void refreshFilters() {
		//		type = ViewType.FILTER;
		//		tiles.clear();
		//		page = 1;
		//		loadPacks(page, getVersion(), getFilter());
	}

	private String getVersion() {
		if (filterVersion.getValue() == null)
			filterVersion.getSelectionModel().selectFirst();
		return filterVersion.getValue();
	}

	private String getFilter() {
		if (filterSort.getValue() == null)
			filterSort.getSelectionModel().selectFirst();
		return filterSort.getValue().getValue();
	}

	public void loadPacks(int page, String version, String sorting) {
		//		new Thread(() -> {
		//			try {
		//				if (pageLoading.get())
		//					return;
		//				pageLoading.set(true);
		//				List<CurseElement> packs = CurseUtils.getPacks(page, version, sorting);
		//				if (packs != null) {
		//					if (!packs.isEmpty()) {
		//						while (!packs.isEmpty()) {
		//							CurseElement pack = packs.remove(0);
		//							Platform.runLater(() -> tiles.add(new CurseModpack(pack)));
		//						}
		//					} else {
		//						lastPage = page;
		//					}
		//				} else {
		//					Platform.runLater(() -> listTiles.setPlaceholder(placeHolderMissing));
		//				}
		//				pageLoading.set(false);
		//			} catch (Exception e) {
		//				OneClientLogging.error(e);
		//			}
		//		}).start();
	}

	public void search() {
		//		new Thread(() -> {
		//			type = ViewType.SEARCH;
		//
		//			List<CurseElement> packs = CurseUtils.searchCurse(textSearch.getText(), "modpacks");
		//			Platform.runLater(() -> {
		//				tiles.clear();
		//				tiles.addAll(packs.stream().map(p -> new CurseModpack(p)).collect(Collectors.toList()));
		//			});
		//		}).start();
	}

	@Override
	public void refresh() {

	}

	@Override
	public void close() {
		//		this.tiles.clear();
	}

	public enum ViewType {
		FILTER,
		SEARCH
	}
}
