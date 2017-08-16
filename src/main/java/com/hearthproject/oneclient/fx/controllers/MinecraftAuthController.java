package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MinecraftAuthController {
	public TextField username;
	public PasswordField password;

	public Instance instance;
	public Stage stage;

	public void login(ActionEvent actionEvent) {
		Platform.runLater(() -> stage.hide());
		if(!MinecraftUtil.startMinecraft(instance, username.getText(), password.getText())){
			stage.show();
			return;
		}
		stage.close();
	}
}
