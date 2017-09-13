package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.hearth.HearthApi;
import com.hearthproject.oneclient.hearth.json.Role;
import com.hearthproject.oneclient.hearth.json.User;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;

public class PrivatePackPane extends ContentPane {

	public Text infoText;
	public Button buttonLogin;

	private User userData;

	public PrivatePackPane() {
		super("gui/contentpanes/private_pack.fxml", "Private Packs (Alpha)", "modpacks.png", ButtonDisplay.TOP);
	}

	@Override
	protected void onStart() {
		buttonLogin.setOnAction(event -> {
			MinecraftAuthController.load();
		});
		buttonLogin.setDisable(!HearthApi.enable);
		if (!HearthApi.enable) {
			infoText.setText("Service unavailable, coming when done.");
		}
	}

	public void onLogin() {
		MiscUtil.runLaterIfNeeded(() -> {
			try {
				userData = HearthApi.getUser();
			} catch (UnirestException e) {
				OneClientLogging.logger.error(e);
			}
			String text = "Welcome " + HearthApi.getAuthentication().username;
			if (userData == null) {
				text = text + "   Failed to load client permissions information";
			} else if (Role.ALPHATESTER.doesUserHaveRole(userData)) {
				text = text + "   Private packs are enabled for you!";
			} else {
				text = text + "   You do not currently have permission to manage private packs!";
			}
			infoText.setText(text);
			buttonLogin.setVisible(false);
		});
	}

	@Override
	public void refresh() {

	}

	@Override
	public boolean showInSideBar() {
		return HearthApi.enable;
	}
}
