package com.hearthproject.oneclient;

import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadTask extends Task<Void> {
	private static final ExecutorService service = Executors.newSingleThreadExecutor();

	private Runnable runnable;

	public DownloadTask(String title, Runnable runnable) {
		updateTitle(title);
		this.runnable = runnable;
	}

	public void start() {
		DownloadManager.DOWNLOADS.put(getTitle(), this);
		ContentPanes.DOWNLOADS_PANE.flashButton();
		service.submit(this);
	}

	@Override
	public void updateMessage(String message) {
		super.updateMessage(message);
	}

	@Override
	public void updateProgress(double workDone, double max) {
		super.updateProgress(workDone, max);
	}

	@Override
	protected Void call() throws Exception {
		this.runnable.run();
		return null;
	}

	@Override
	protected void succeeded() {
		updateMessage("Finished Task " + getTitle());
		super.succeeded();
	}
}
