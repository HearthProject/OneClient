package com.hearthproject.oneclient.api.modpack;

import com.google.common.collect.Maps;
import com.hearthproject.oneclient.api.modpack.curse.CurseExporter;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;

import java.util.HashMap;
import java.util.Map;

public class ModpackManager {

	public static final HashMap<String, IExporter> EXPORTERS = Maps.newHashMap();

	static {
		EXPORTERS.put("Curse ZIP", new CurseExporter());
		EXPORTERS.put("Modlist txt", new ModlistExporter());
	}

	public static void createExporters(ObservableList<MenuItem> items, Instance instance) {
		for (Map.Entry<String, IExporter> entry : EXPORTERS.entrySet()) {
			MenuItem item = new MenuItem(entry.getKey());
			item.setOnAction(event -> entry.getValue().export(instance));
			items.add(item);
		}
	}

}
