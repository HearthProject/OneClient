package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class DownloadManager {
	public static ObservableMap<String, DownloadTask> DOWNLOADS = FXCollections.observableHashMap();

	public static DownloadTask createDownload(String name, Runnable runnable) {
		if (DOWNLOADS.containsKey(name))
			return get(name);
		return new DownloadTask(name, runnable);
	}

	public static ReadOnlyStringProperty messageProperty(String name) {
		return get(name).messageProperty();
	}

	public static void updateMessage(String name, String message, Object... params) {
		if (get(name) != null) {
			get(name).updateMessage(String.format(message, params));
			OneClientLogging.info(String.format(message, params));
		}
	}

	public static ReadOnlyDoubleProperty progressProperty(String name) {
		return get(name).progressProperty();
	}

	public static void updateProgress(String name, double workDone, double max) {
		if (get(name) != null)
			get(name).updateProgress(workDone, max);
	}

	public static DownloadTask get(String name) {
		return DOWNLOADS.get(name);
	}
}
