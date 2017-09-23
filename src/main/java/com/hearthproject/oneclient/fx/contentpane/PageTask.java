package com.hearthproject.oneclient.fx.contentpane;

import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

public abstract class PageTask<T> extends Task<Void> {
	private final int loadPerScroll = 10;

	private List<Map.Entry<String, CurseProject>> entries;
	private ObservableList<T> tiles;
	private StringProperty placeholder;

	public PageTask(List<Map.Entry<String, CurseProject>> entries,
	                ObservableList<T> tiles,
	                StringProperty placeholder) {
		this.entries = entries;
		this.tiles = tiles;
		this.placeholder = placeholder;
	}

	public abstract void addElement(List<T> elements, Map.Entry<String, CurseProject> entry);

	@Override
	protected Void call() throws Exception {
		if (checkCancel())
			return null;
		OneClientLogging.info("Loading page");
		List<T> elements = Lists.newArrayList();
		for (int i = 0; i < loadPerScroll; i++) {
			if (checkCancel() || entries == null || entries.isEmpty())
				break;

			Map.Entry<String, CurseProject> entry = entries.remove(0);
			addElement(elements, entry);
			OneClientLogging.info("Element Created");
		}
		if (checkCancel())
			return null;
		MiscUtil.runLaterIfNeeded(() -> tiles.addAll(elements));
		if (checkCancel())
			return null;
		//		OneClientLogging.info("Loaded {} of {} Modpacks", count - entries.size(), count);
		if (tiles.isEmpty()) {
			MiscUtil.runLaterIfNeeded(() -> placeholder.setValue("No Packs Found"));
		}
		return null;
	}

	private boolean checkCancel() {
		if (Thread.currentThread().isInterrupted()) {
			return true;
		}
		return false;
	}

}
