package com.hearthproject.oneclient.hearth.fx;

import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.hearth.api.HearthApi;
import com.hearthproject.oneclient.hearth.api.json.packs.ModPack;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;


public class NewPackPane extends ContentPane {

	public TextField textPackName;
	public Text detailLable;
	public Button buttonCreate;

	public NewPackPane() {
		super("gui/hearth/new_pack.fxml", "New Pack", "", ButtonDisplay.NONE);
	}

	@Override
	protected void onStart() {
		textPackName.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				detailLable.setVisible(!HearthApi.getHearthPrivatePacks().isValid(textPackName.getText()));
			} catch (Exception e) {
				OneClientLogging.error(e);
			}
		});
		buttonCreate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					ModPack modPack = HearthApi.getHearthPrivatePacks().createNewPack(textPackName.getText(), "Testing mod pack");
					System.out.println(modPack);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void refresh() {

	}
}
