package com.hearthproject.oneclient;

import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
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
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

	public static Stage stage;
	public static MainController mainController;
	public static Scene scene;
	public static List<String> args;
	public static boolean nativeLaunch = false;

	public static void main(String... argArray) throws IOException {
		Main.args = Arrays.asList(argArray);
		launch(argArray);
	}

	@Override
	public void start(Stage s) throws IOException {
		stage = s;
		Constants.earlySetup(this::startOneClient);
	}

	public void startOneClient() {
		SettingsUtil.init();
		OneClientLogging.logger.info("Starting OneClient version: " + Constants.getVersion());
		OneClientTracking.sendRequest("launch/" + Constants.getVersion());
		Platform.runLater(() -> {
			OneClientLogging.setupLogController();
			if (SettingsUtil.settings.show_log_window)
				OneClientLogging.showLogWindow();
			try {
				SplashScreen.show();
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
			for (String arg : args) {
				if (arg.equals("-updateSuccess")) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Update complete!");
					alert.setHeaderText("The update was successful!");
					alert.setContentText("You are now running OneClient version " + Constants.getVersion() + "!");
					alert.showAndWait();
				} else if (arg.equals("-native_launch")) {
					nativeLaunch = true;
				}
			}
		});
		new Thread(() -> {
			try {
				loadData();
				SplashScreen.hide();
				Platform.runLater(() -> {
					try {
						startLauncher();
						Optional<String> latestVersion = Updater.checkForUpdate();
						if (latestVersion.isPresent() && !nativeLaunch) {
							Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
							alert.setTitle("Update?");
							alert.setContentText("OneClient version " + latestVersion.get() + " is available, you are using " + Constants.getVersion() + ". Would you like to update?");
							alert.setHeaderText("An update is available!");
							Optional<ButtonType> result = alert.showAndWait();
							if (result.get() == ButtonType.OK) {
								stage.hide();
								SplashScreen.show();
								SplashScreen.updateProgess("Downloading update...", 0);
								new Thread(Updater::startUpdate).start();
							}
						}
					} catch (Exception e) {
						OneClientLogging.error(e);
					}
				});
			} catch (Exception e) {
				OneClientLogging.error(e);
			}
		}).start();
	}

	public void startLauncher() throws Exception {
		OneClientLogging.logger.info("Starting One Client");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL fxmlUrl = classLoader.getResource("gui/main.fxml");
		if (fxmlUrl == null) {
			OneClientLogging.logger.error("An error has occurred loading main.fxml!");
			return;
		}
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(fxmlUrl);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		Parent root = fxmlLoader.load(fxmlUrl.openStream());
		if (Constants.getVersion() == null) {
			stage.setTitle("One Client");
		} else {
			stage.setTitle("One Client " + Constants.getVersion());
		}
		stage.getIcons().add(new Image("images/icon.png"));
		scene = new Scene(root, 1288, 800);
		scene.getStylesheets().add("gui/css/theme.css");
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest(event -> {
			OneClientLogging.stage.close();
			OneClientLogging.logger.info("Goodbye");
			System.exit(0);
		});
		mainController = fxmlLoader.getController();
		scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
		scene.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
		mainController.onStart(stage);
		MinecraftAuthController.load();
	}

	public void loadData() throws Exception {
		System.setProperty("http.agent", "OneClient/1.0");
		checkModpackIcon();
		InstanceManager.load();
		OneClientLogging.logger.info("Loading Minecraft versions");
		MinecraftUtil.load();
		OneClientLogging.logger.info("Loading Forge versions");
		ForgeUtils.loadForgeVersions();
		OneClientLogging.logger.info("Loading Curse versions and sorting");
		SplashScreen.updateProgess("Authenticating with mojang", 90);
		OneClientLogging.logger.info("Done!");
		SplashScreen.updateProgess("Done!", 100);
	}

	private static void checkModpackIcon(){
		if(!Constants.MODPACKICON.exists()){
			try {
				FileUtils.copyInputStreamToFile(FileUtil.getResource("images/modpack.png"), Constants.MODPACKICON);
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
		}
	}
}
