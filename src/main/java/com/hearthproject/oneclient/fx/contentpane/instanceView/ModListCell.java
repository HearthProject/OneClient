package com.hearthproject.oneclient.fx.contentpane.instanceView;

import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class ModListCell extends ListCell<String> {
	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if(item != null && item.endsWith(".disabled")){
			this.setTextFill(Color.RED);
		} else {
			this.setTextFill(Color.BLACK);
		}
	}
}
