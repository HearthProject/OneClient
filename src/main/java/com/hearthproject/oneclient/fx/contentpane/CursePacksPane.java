package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.IOException;

public class CursePacksPane extends ContentPane {
	public WebView webView;
	public Button buttonBrowse;
	public Button buttonInstall;

	public CursePacksPane() {
		super("gui/contentpanes/getCurseContent.fxml", "Get Curse Pack", "#DB6B20");
	}

	@Override
	protected void onStart() {
		webView.getEngine().load("https://minecraft.curseforge.com/modpacks");
		webView.minWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		webView.minHeightProperty().bind(Main.mainController.contentBox.heightProperty());

		VBox box = (VBox) getNode();
		box.maxWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		box.maxHeightProperty().bind(Main.mainController.contentBox.heightProperty());

		webView.maxWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		webView.maxHeightProperty().bind(Main.mainController.contentBox.heightProperty());

		WebEngine webEngine = webView.getEngine();

		buttonBrowse.setOnAction(event -> webView.getEngine().load("https://minecraft.curseforge.com/modpacks"));

		buttonInstall.setOnAction(event -> {
			System.out.println("installing");

			String url = webEngine.getLocation();
			if (!url.startsWith("https://minecraft.curseforge.com/projects/")) {
				System.out.println("This is not a pack!");
				return;
			}

			try {
				InstallingController.showInstaller();
			} catch (IOException e) {
				e.printStackTrace();
			}

			InstallingController.controller.setTitleText("Installing...");
			InstallingController.controller.setDetailText("Preparing to install");

			new Thread(() -> {
				Instance instance = new Instance("Unknown");
				try {
					new CursePackInstaller().downloadFromURL(url, "latest", instance);
					MinecraftUtil.installMinecraft(instance);
				} catch (Throwable throwable) {
					OneClientLogging.log(throwable);
				}
				InstanceManager.addInstance(instance);
				if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
					Main.mainController.currentContent.refresh();
				}
				//TODO show instances
			}).start();
		});
	}

	@Override
	public void refresh() {

	}
}
