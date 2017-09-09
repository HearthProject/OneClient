package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ContentPaneButton extends JFXButton {
	private boolean selected = false;
	private ButtonDisplay buttonDisplay;

	public ContentPaneButton(String imageName, ButtonDisplay buttonDisplay) {
		super("TEST");
		ImageView imageView = new ImageView();
		if (imageName != null && !imageName.isEmpty()) {
			Image image = ImageUtil.openCachedImage(FileUtil.getResource("images/" + imageName));
			if (image != null)
				imageView.setImage(image);
			setGraphic(imageView);
		}
		setAlignment(Pos.CENTER_LEFT);
		setPrefHeight(50);
		setFocusTraversable(false);
		setId("oc-panel-button");
		this.buttonDisplay = buttonDisplay;
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

	public ButtonDisplay getButtonDisplay() {
		return buttonDisplay;
	}

	private void updateColor() {
		if (selected) {
			setStyle("-fx-background-color: -oc-accent");
		} else {
			setStyle(null);
		}
	}
}
