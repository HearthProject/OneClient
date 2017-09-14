package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.api.curse.CurseImporter;
import com.hearthproject.oneclient.api.curse.data.CurseModpacks;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.InstallTile;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.ScrollEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurseMetaPane extends ContentPane {
	public Label placeholder;
	public ObservableList<InstallTile> tiles = FXCollections.observableArrayList();
	public ListView<InstallTile> listPacks;
	private int loadPerScroll = 10;

	public CurseMetaPane() {
		super("gui/contentpanes/curse_packs.fxml", "Get Modpacks2", "modpacks.png", ButtonDisplay.TOP);
	}

	public final EventHandler<ScrollEvent> scroll = event -> {
		if (event.getDeltaY() < 0) {
			loadPacks(loadPerScroll);
		}
	};

	private static List<Map.Entry<String, CurseModpacks.CurseModpack>> entries;

	private volatile SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@Override
	protected void onStart() {
		CurseModpacks packs = Curse.getModpacks();
		if (packs != null) {
			entries = Lists.newArrayList(packs.entrySet());
			entries.sort(Comparator.comparing(e -> e.getValue().Name));
		}
		listPacks.setPlaceholder(placeholder = new Label("Loading..."));
		NotifyUtil.loadingIcon().visibleProperty().bind(loading);
		listPacks.setOnScroll(scroll);
		listPacks.setItems(tiles);
		loadPacks(loadPerScroll);
	}

	public void loadPacks(int count) {
		if (loading.get()) {
			OneClientLogging.info("already loading");
			return;
		}
		new Thread(() -> {
			loading.setValue(true);
			OneClientLogging.info("Loading more");
			List<Instance> instances = Lists.newArrayList();
			for (int i = 0; i < count; i++) {
				Map.Entry<String, CurseModpacks.CurseModpack> entry = entries.remove(0);
				//TODO game version filtering
				Instance instance = new CurseImporter(entry.getKey(), "1.12").create();
				instances.add(instance);
			}
			MiscUtil.runLaterIfNeeded(() -> tiles.addAll(instances.stream().map(InstallTile::new).collect(Collectors.toList())));
			loading.setValue(false);
		}).start();
	}

	@Override
	public void refresh() {
		tiles.clear();
	}

}
