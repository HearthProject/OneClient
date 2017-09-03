package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.nodes.CurseMod;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class ModInstallingController {

	public Instance instance;
	public ObservableList<CurseMod> tiles = FXCollections.observableArrayList();
	public ObservableList<CurseUtils.Filter> sorting;

	public ListView<CurseMod> listTiles;
	public ComboBox<CurseUtils.Filter> filterSort;

	public int page;

	public static ModInstallingController controller;
	public static Stage stage;

	public static void showInstaller(Instance instance) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/contentpanes/curse_mods.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.logger.error("An error has occurred loading gui/contentpanes/curse_mods.fxml!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent root = fxmlLoader.load(fxmlUrl.openStream());

			stage = new Stage();
			stage.setTitle("Modpack List");
			stage.setResizable(false);
			stage.initOwner(Main.stage);
			stage.initModality(Modality.WINDOW_MODAL);
			Scene scene = new Scene(root, 600, 500);
			scene.getStylesheets().add("gui/css/theme.css");
			stage.setScene(scene);
			stage.show();
			controller = fxmlLoader.getController();
			controller.sorting = FXCollections.observableArrayList(CurseUtils.getSorting());
			controller.instance = instance;

			controller.filterSort.setItems(controller.sorting);
			controller.filterSort.getSelectionModel().selectFirst();
			controller.filterSort.valueProperty().addListener((observableValue, s, t1) -> controller.refreshFilters());
			controller.filterSort.setConverter(new CurseUtils.FilterConverter());

			controller.loadModPage(1, controller.getFilter());
			controller.listTiles.setItems(controller.tiles);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
	}

	public static void close() {
		if (Platform.isFxApplicationThread()) {
			stage.hide();
			stage.close();
		} else {
			Platform.runLater(() -> {
				stage.hide();
				stage.close();
			});
		}
		OneClientLogging.logger.error("Closing");
	}

	public void loadModPage(int page, String sorting) {
		List<CurseElement> elementList = CurseUtils.getMods(page, instance.getManifest().getMinecraftVersion(), sorting);
		controller.tiles.addAll(elementList.stream().map(element -> new CurseMod(instance, element)).collect(Collectors.toList()));
	}

	public void refreshFilters() {
		tiles.clear();
		page = 1;
		loadModPage(page, getFilter());
	}

	private String getFilter() {
		if (filterSort.getValue() == null)
			filterSort.getSelectionModel().selectFirst();
		return filterSort.getValue().getValue();
	}

}
