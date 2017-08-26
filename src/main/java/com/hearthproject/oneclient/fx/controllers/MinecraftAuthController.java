package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.AuthStore;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.URL;

public class MinecraftAuthController {
	public TextField username;
	public PasswordField password;

	public Instance instance;
	public Stage stage;
	public Button buttonLogin;
	public CheckBox checkboxPasswordSave;

	public void login(ActionEvent actionEvent) {
		stage.hide();
		save();
		if (!MinecraftUtil.startMinecraft(instance, username.getText(), password.getText())) {
			stage.show();
			return;
		}
		stage.close();
	}

	public void load() {
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
			OneClientLogging.log(e);
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
			OneClientLogging.log(e);
		}

	}

	public void onLinkClick() {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/HearthProject/OneClient").toURI());
		} catch (Exception e) {
			OneClientLogging.log(e);
		}
	}

	public File getAuthStore() {
		return new File(Constants.getRunDir(), "authstore.dat");
	}

}
