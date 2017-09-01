package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.jfoenix.controls.JFXButton;
import javafx.scene.image.ImageView;

public class ContentPaneButton extends JFXButton {
	private boolean selected = false;
	private String color;

	public ContentPaneButton(String imageName) {
		super("TEST");
		ImageView imageView = new ImageView();
		imageView.setImage(ImageUtil.openImage(FileUtil.getResource("images/" + imageName)));
		setGraphic(imageView);
		this.color = "#3CE0A0";
		setPrefHeight(50);
		setId("side-panel-button");
	}

	@Override
	public ButtonType getButtonType() {
		return ButtonType.FLAT;
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
