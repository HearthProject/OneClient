package com.hearthproject.oneclient.fx.controllers.content;

import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.json.models.curse.ModPacks;
import com.hearthproject.oneclient.json.models.launcher.ModPack;
import com.hearthproject.oneclient.util.curse.CurseUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputMethodEvent;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PackPaneHeader extends ContentPaneController {
	public TextField searchBox;

	static Map<Integer, Image> imageMap = new HashMap<>();
	static boolean search;
	static String seachTerm;
	static boolean canceUpdate = false;

	static MainController staticController;

	@Override
	protected void onStart() {
		staticController = controller;
		updatePackList();
		searchBox.textProperty().addListener((observable, oldValue, newValue) -> updatePackList());
	}

	static Thread reloadThread = createUpdateThread();

	private static Thread createUpdateThread() {
		return new Thread(() -> {
			try {
				try {
					int i = 0;
					for(ModPacks.CursePack cursePack : CurseUtil.loadModPacks().Data){
						if(canceUpdate){
							break;
						}
						if(search){
							if(!cursePack.Name.toLowerCase().contains(seachTerm.toLowerCase())){
								continue;
							}
						}
						if(i > 50){
							break;
						}
						i++;
						ModPack pack = new ModPack(cursePack);
						Image image;
						if(imageMap.containsKey(cursePack.Id)){
							image = imageMap.get(cursePack.Id);
						} else {
							image = new Image(new URL(pack.iconUrl).openStream());
						}
						if(canceUpdate){
							break;
						}
						Platform.runLater(() -> addPackCard(pack, image));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				OneClientLogging.log(e);
			}
		});
	}

	public void updatePackList(){
		search = !searchBox.getText().isEmpty();
		seachTerm = searchBox.getText();
		Node node = controller.contentPane.getChildren().get(0);
		controller.contentPane.getChildren().clear();
		controller.contentPane.getChildren().add(node);
		canceUpdate = true;
		try {
			reloadThread.join();
		} catch (InterruptedException e) {
			OneClientLogging.log(e);
		}
		canceUpdate = false;
		reloadThread = createUpdateThread();
		reloadThread.start();
	}

	@Override
	public void refresh() {

	}

	public void seachChanged(InputMethodEvent inputMethodEvent) {

	}

	public static void addPackCard(ModPack modPack, Image image){
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
			staticController.contentPane.getChildren().add(fxmlLoader.load(fxmlUrl.openStream()));
			PackCardController packCardController = fxmlLoader.getController();
			packCardController.modpackName.setText(modPack.name);
			packCardController.modpackDetails.setText(modPack.authors);
			packCardController.modpackDescription.setText(modPack.description);
			if(image != null){
				packCardController.modpackImage.setImage(image);
			} else {
				packCardController.modpackImage.setImage(new Image(new URL(modPack.iconUrl).openStream()));
			}


		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}
}
