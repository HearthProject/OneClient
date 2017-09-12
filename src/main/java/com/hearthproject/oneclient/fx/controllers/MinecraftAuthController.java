package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.HearthInstance;
import com.hearthproject.oneclient.hearth.HearthApi;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.AuthStore;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.*;
import java.net.URL;

public class MinecraftAuthController {

	public TextField username;
	public PasswordField password;

	public HearthInstance instance;
	public Stage stage;
	public Button buttonLogin;
	public CheckBox checkboxPasswordSave;

	private boolean minecraft;

	public void login(ActionEvent actionEvent) {
		stage.hide();
		save();
		if (minecraft) {
			if (!MinecraftUtil.startMinecraft(instance, username.getText(), password.getText())) {
				stage.show();
				return;
			}
			NotifyUtil.setText(Duration.seconds(2), "%s successfully launching!", instance.getName());
		} else {
			try {
				if (!HearthApi.login(username.getText(), password.getText())) {
					stage.show();
					return;
				}
				HearthApi.getClientPermissions();
			} catch (UnirestException e) {
				OneClientLogging.logger.error(e);
			}
		}
		stage.close();
	}

	public void load(boolean minecraft) {
		this.minecraft = minecraft;
		try {
			if (getAuthStore().exists()) {
				FileInputStream inputStream = new FileInputStream(getAuthStore());
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
				AuthStore authStore = (AuthStore) objectInputStream.readObject();
				objectInputStream.close();
				inputStream.close();
				username.setText(authStore.username);
				password.setText(authStore.password);
				checkboxPasswordSave.setSelected(true);
			} else {
				checkboxPasswordSave.setSelected(false);
			}
		} catch (Exception e) {
			OneClientLogging.error(e);
			checkboxPasswordSave.setSelected(false);
		}

	}

	public void save() {
		try {
			if (checkboxPasswordSave.isSelected()) {
				FileOutputStream fileOutputStream = new FileOutputStream(getAuthStore());
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				AuthStore authStore = new AuthStore();
				authStore.username = username.getText();
				authStore.password = password.getText();
				objectOutputStream.writeObject(authStore);
				objectOutputStream.close();
				fileOutputStream.close();
			} else {
				if (getAuthStore().exists()) {
					getAuthStore().delete();
				}
			}
		} catch (Exception e) {
			OneClientLogging.error(e);
		}

	}

	public void onLinkClick() {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/HearthProject/OneClient").toURI());
		} catch (Exception e) {
			OneClientLogging.error(e);
		}
	}

	public File getAuthStore() {
		return new File(Constants.getRunDir(), "authstore.dat");
	}

}
