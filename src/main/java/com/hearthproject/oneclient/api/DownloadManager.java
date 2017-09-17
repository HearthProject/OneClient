package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.DownloadTask;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class DownloadManager {
	public static ObservableMap<String, DownloadTask> DOWNLOADS = FXCollections.observableHashMap();

	public static void startDownload(String name, Runnable runnable) {
		DOWNLOADS.put(name, new DownloadTask(name, runnable).start());
	}

	public static ReadOnlyStringProperty messageProperty(String name) {
		return get(name).messageProperty();
	}

	public static void updateMessage(String name, String message, Object... params) {
		get(name).updateMessage(String.format(message, params));
	}

	public static ReadOnlyDoubleProperty progressProperty(String name) {
		return get(name).progressProperty();
	}

	public static void updateProgress(String name, double workDone, double max) {
		get(name).updateProgress(workDone, max);
	}

	public static DownloadTask get(String name) {
		return DOWNLOADS.get(name);
	}
}
