package com.hearthproject.oneclient.fx.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class LogController {
	public TextArea logArea;

	private Stage stage;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void menuUpload(ActionEvent actionEvent) {

	}

	public void menuClose(ActionEvent actionEvent) {
		stage.hide();
	}
}
