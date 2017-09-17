package com.hearthproject.oneclient;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadTask extends Task<Void> {
	private ExecutorService service = Executors.newSingleThreadExecutor();

	private String name;
	private Runnable runnable;

	public DownloadTask(String name, Runnable runnable) {
		this.name = name;
		this.runnable = runnable;
	}

	public DownloadTask start() {
		service.submit(this);
		return this;
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

	public String getName() {
		return name;
	}
}
