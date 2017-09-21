package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.hearth.api.HearthApi;
import com.hearthproject.oneclient.hearth.api.json.Role;
import com.hearthproject.oneclient.hearth.api.json.User;
import com.hearthproject.oneclient.hearth.api.json.packs.ModPack;
import com.hearthproject.oneclient.hearth.fx.HearthPanes;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Text;

import java.util.List;

public class PrivatePackPane extends ContentPane {

	public Text infoText;
	public Button buttonLogin;
	public Button buttonNewPack;
	public Button buttonInstall;
	public Button buttonEdit;
	public ChoiceBox packList;
	public ChoiceBox adminPackList;

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
		buttonNewPack.setOnAction(event -> HearthPanes.newPackPane.button.fire());
	}

	public void onLogin() {
		MiscUtil.runLaterIfNeeded(() -> {
			try {
				userData = HearthApi.getHearthAuthentication().getUser();
			} catch (UnirestException e) {
				OneClientLogging.logger.error(e);
			}
			String text = "Welcome " + HearthApi.getHearthAuthentication().getAuthentication().username;
			if (userData == null) {
				text = text + "   Failed to load client permissions information";
			} else if (Role.ALPHA_TESTER.doesUserHaveRole(userData)) {
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
		packList.getItems().clear();
		adminPackList.getItems().clear();
		if (HearthApi.enable) {
			try {
				List<ModPack> packs = HearthApi.getHearthPrivatePacks().getPacks();
				if (!packs.isEmpty()) {
					for (ModPack modPack : packs) {
						packList.getItems().add(modPack.name);
					}
					packList.getSelectionModel().select(0);
				}

				List<ModPack> admin = HearthApi.getHearthPrivatePacks().getAdminPacks();
				if (!admin.isEmpty()) {
					for (ModPack modPack : admin) {
						adminPackList.getItems().add(modPack.name);
					}
					adminPackList.getSelectionModel().select(0);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean showInSideBar() {
		return HearthApi.enable;
	}
}
