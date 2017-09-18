package com.hearthproject.oneclient;

import com.hearthproject.oneclient.api.DownloadManager;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadTask extends Task<Void> {
	private static final ExecutorService service = Executors.newSingleThreadExecutor();

	private String name;
	private Runnable runnable;
	private boolean removed;

	public DownloadTask(String name, Runnable runnable) {
		this.name = name;
		this.runnable = runnable;
	}

	public void start() {
		DownloadManager.DOWNLOADS.put(name, this);
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
	protected void done() {
		updateMessage("Finished Installed!");
		updateProgress(1, 1);
		super.done();
	}

	public String getName() {
		return name;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
	}

}
