package com.hearthproject.oneclient;

import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.util.curse.CursePackUtil;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.launcher.PackUtil;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.launcher.Updater;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.hearthproject.oneclient.util.tracking.OneClientTracking;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class Main extends Application {

	public static Stage stage;
	public static MainController mainController;

	public static void main(String... args) {
		SettingsUtil.init();
		OneClientLogging.log("Starting OneClient version: " + Constants.getVersion());
		if (Constants.CUSTOM_RUN) {
			OneClientLogging.log("Using custom run dir: " + Constants.getRunDir().getAbsolutePath());
		}
		OneClientTracking.sendRequest("launch/" + Constants.getVersion());
		Platform.runLater(() -> {
			OneClientLogging.setupLogController();
			if (SettingsUtil.settings.show_log_window)
				OneClientLogging.showLogWindow();
			try {
				SplashScreen.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (String arg : args) {
				if (arg.equals("-updateSuccess")) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Update complete!");
					alert.setHeaderText("The update was successful!");
					alert.setContentText("You are now running OneClient version " + Constants.getVersion() + "!");
					alert.showAndWait();
				}
			}
		});
		launch(args);
	}

	@Override
	public void start(Stage s) {
		stage = s;
		new Thread(() -> {
			try {
				loadData();
				SplashScreen.hide();
				Platform.runLater(() -> {
					try {
						startLauncher();
						Optional<String> latestVersion = Updater.checkForUpdate();
						if (latestVersion.isPresent()) {
							Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
							alert.setTitle("Update?");
							alert.setContentText("OneClient version " + latestVersion.get() + " is available, you are using " + Constants.getVersion() + ". Would you like to update?");
							alert.setHeaderText("An update is available!");
							Optional<ButtonType> result = alert.showAndWait();
							if (result.get() == ButtonType.OK) {
								Updater.startUpdate();
							}
						}
					} catch (Exception e) {
						OneClientLogging.log(e);
					}
				});
			} catch (Exception e) {
				OneClientLogging.log(e);
			}
		}).start();
	}

	public void startLauncher() throws Exception {
		OneClientLogging.log("Starting One Client");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL fxmlUrl = classLoader.getResource("gui/main.fxml");
		if (fxmlUrl == null) {
			OneClientLogging.log("An error has occurred loading main.fxml!");
			return;
		}
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(fxmlUrl);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		Parent root = fxmlLoader.load(fxmlUrl.openStream());
		stage.setTitle("One Client " + Constants.getVersion());
		stage.getIcons().add(new Image("icon.png"));
		Scene scene = new Scene(root, 1200, 800);
		scene.getStylesheets().add("gui/css/theme.css");
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest((windowEvent) -> OneClientLogging.stage.close());
		mainController = fxmlLoader.getController();
		scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
		scene.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
		mainController.onStart(stage);

	}

	public void loadData() throws Exception {
		OneClientLogging.log("Loading instances");
		InstanceManager.load();
		OneClientLogging.log("Loading minecraft versions");
		MinecraftUtil.loadGameVersions();
		OneClientLogging.log("Loading forge versions");
		ForgeUtils.loadForgeVersions();
		OneClientLogging.log("Loading mod packs");
		PackUtil.loadModPacks();
		OneClientLogging.log("Loading curse mod packs");
		CursePackUtil.loadModPacks();
		OneClientLogging.log("Done!");
		SplashScreen.updateProgess("Done!", 100);
	}
}
