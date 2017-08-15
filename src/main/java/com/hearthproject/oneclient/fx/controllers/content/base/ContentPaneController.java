package com.hearthproject.oneclient.fx.controllers.content.base;

import com.hearthproject.oneclient.fx.controllers.MainController;

public abstract class ContentPaneController {
	public MainController controller;

	protected abstract void onStart();

	public abstract void refresh();

	public final void start() {
		onStart();
		refresh();
	}
}
