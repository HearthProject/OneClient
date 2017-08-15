package com.hearthproject.oneclient.fx.controllers.content;

import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.json.models.launcher.ModPack;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputMethodEvent;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

public class PackPaneHeader extends ContentPaneController {
	public TextField searchBox;

	@Override
	protected void onStart() {
		ModPack modPack = new ModPack();
		modPack.name = "Test Pack";
		modPack.authors = "by modmuss50";
		modPack.iconUrl = "https://media-elerium.cursecdn.com/avatars/73/795/636163746946033291.png";
		addPackCard(modPack);

		ModPack modPack2 = new ModPack();
		modPack2.name = "FTB Unstable 1.12";
		modPack2.authors = "by FTB";
		modPack2.description = "The official bleeding-edge testing platform used by Feed The Beast for their more modern 1.12.X packs. Highly experimental and volatile. Just a reminder: no tech support will be offered for this pack!\n";
		modPack2.iconUrl = "https://media-elerium.cursecdn.com/avatars/thumbnails/105/433/340/340/636352866910284865.png";
		addPackCard(modPack2);
		addPackCard(modPack2);
	}

	@Override
	public void refresh() {

	}

	public void seachChanged(InputMethodEvent inputMethodEvent) {

	}

	public void addPackCard(ModPack modPack){
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/modpacklist/packcard.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.log("An error has occurred loading the mod pack card!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			controller.contentPane.getChildren().add(fxmlLoader.load(fxmlUrl.openStream()));
			PackCardController packCardController = fxmlLoader.getController();
			packCardController.modpackName.setText(modPack.name);
			packCardController.modpackDetails.setText(modPack.authors);
			packCardController.modpackDescription.setText(modPack.description);
			packCardController.modpackImage.setImage(new Image(new URL(modPack.iconUrl).openStream()));

		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}
}
