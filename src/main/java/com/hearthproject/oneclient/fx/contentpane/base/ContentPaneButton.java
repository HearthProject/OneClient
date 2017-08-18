package com.hearthproject.oneclient.fx.contentpane.base;

import javafx.scene.control.Button;

public class ContentPaneButton extends Button {
	private boolean selected = false;
	private String color;

	public ContentPaneButton(String color) {
		this.color = color;
		setPrefHeight(130);
		setId("side-panel-button");
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		updateColor();
	}

	private void updateColor() {
		if (selected) {
			setStyle("-fx-background-color: " + color);
		} else {
			setStyle(null);
		}
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
		updateColor();
	}
}
