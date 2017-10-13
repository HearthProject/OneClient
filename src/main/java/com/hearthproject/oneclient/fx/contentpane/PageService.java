package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.api.cmdb.Database;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.Executors;

public abstract class PageService<T> extends Service<Void> {
    protected String type;
    protected ObservableList<T> tiles;
    protected StringProperty placeholder;
    protected IntegerProperty count;
    private List<Database.Project> entries;

    public PageService(List<Database.Project> entries,
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
