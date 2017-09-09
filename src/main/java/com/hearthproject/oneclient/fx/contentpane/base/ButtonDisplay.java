package com.hearthproject.oneclient.fx.contentpane.base;

import com.hearthproject.oneclient.Main;
import javafx.scene.layout.VBox;

public enum ButtonDisplay {
	TOP(Main.mainController.topButtonBox), ABOVE_DIVIDER(Main.mainController.aboveDividerButtonBox), BELOW_DIVIDER(Main.mainController.belowDividerButtonBox), NONE(null);

	VBox container;

	ButtonDisplay(VBox container) {
		this.container = container;
	}

	public VBox getContainer() {
		return container;
	}
}
