package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.api.curse.CurseImporter;
import com.hearthproject.oneclient.api.curse.data.CurseModpacks;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.MiscUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Map;

public class CurseMetaPane extends ContentPane {

	public ObservableList<Instance> instances = FXCollections.observableArrayList();
	public TableView<Instance> tablePacks;

	public CurseMetaPane() {
		super("gui/contentpanes/curse_packs.fxml", "Get Modpacks2", "modpacks.png", ButtonDisplay.TOP);
	}

	public Button createButton(Instance instance) {
		Button button = new Button("Install");
		button.setOnAction(e -> new Thread(instance::install).start());
		return button;
	}

	@Override
	protected void onStart() {

		TableColumn<Instance, String> name = new TableColumn<>("name");
		name.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));

		TableColumn<Instance, Button> install = new TableColumn<>("install");
		install.setCellValueFactory(cell -> new SimpleObjectProperty<>(createButton(cell.getValue())));
		tablePacks.setItems(instances);
		tablePacks.setFixedCellSize(30);
		new Thread(() -> {
			//TODO idk scrolling shit
			CurseModpacks packs = Curse.getModpacks();
			System.out.println(tablePacks.getHeight() / 30d);
			for (int i = 0; i < tablePacks.getHeight() / 30d; i++) {
				Map.Entry<String, CurseModpacks.CurseModpack> entry = Lists.newArrayList(packs.entrySet()).get(i);
				Instance instance = new CurseImporter(entry.getKey(), "1.12").create();
				System.out.println(instance.getName());
				instances.add(instance);
			}
			MiscUtil.runLaterIfNeeded(() -> tablePacks.setItems(instances));
		}).start();

		tablePacks.getColumns().addAll(install, name);
	}

	@Override
	public void refresh() {

	}
}
