package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.api.modpack.curse.data.CurseProject;
import javafx.beans.property.IntegerProperty;
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
	protected IntegerProperty count;

	public PageService(Supplier<List<Map.Entry<String, CurseProject>>> entries,
	                   ObservableList<T> tiles,
	                   StringProperty placeholder, IntegerProperty count) {
		this.entries = entries;
		this.tiles = tiles;
		this.placeholder = placeholder;
		this.count = count;
		this.setExecutor(Executors.newSingleThreadExecutor());
	}

	@Override
	protected abstract Task<Void> createTask();

}
