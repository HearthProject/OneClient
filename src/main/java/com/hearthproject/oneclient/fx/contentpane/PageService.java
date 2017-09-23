package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.api.curse.data.CurseProject;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public abstract class PageService<T> extends Service<Void> {
	protected Supplier<List<Map.Entry<String, CurseProject>>> entries;
	protected ObservableList<T> tiles;
	protected StringProperty placeholder;

	public PageService(Supplier<List<Map.Entry<String, CurseProject>>> entries,
	                   ObservableList<T> tiles,
	                   StringProperty placeholder) {
		this.entries = entries;
		this.tiles = tiles;
		this.placeholder = placeholder;
		this.setExecutor(Executors.newSingleThreadExecutor());
	}

	@Override
	protected abstract Task<Void> createTask();

}
