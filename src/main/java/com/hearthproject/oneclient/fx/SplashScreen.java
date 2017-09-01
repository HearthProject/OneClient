package com.hearthproject.oneclient.fx;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.controllers.SplashScreenController;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SplashScreen {

	public static Stage stage;
	static SplashScreenController splashScreenController;
	public static boolean loaded = false;

	public static void show() throws IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL fxmlUrl = classLoader.getResource("gui/splash.fxml");
		if (fxmlUrl == null) {
			OneClientLogging.logger.error("An error has occurred loading instance_creation.fxml!");
			return;
		}
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(fxmlUrl);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		Parent root = fxmlLoader.load(fxmlUrl.openStream());
		stage = new Stage();
		stage.setTitle("One Client - Loading");
		stage.getIcons().add(new Image("images/icon.png"));
		stage.setResizable(false);
		stage.initOwner(Main.stage);
		stage.initModality(Modality.WINDOW_MODAL);
		Scene scene = new Scene(root, 600, 400);
		scene.getStylesheets().add("gui/css/theme.css");
		stage.setScene(scene);
		splashScreenController = fxmlLoader.getController();
		stage.show();
		loaded = true;
	}

	public static void updateProgess(String text, double percentage) {
		if (!loaded) {
			return;
		}
		Platform.runLater(() -> {
			splashScreenController.progressBar.setProgress(percentage / 100);
			splashScreenController.progressText.setText(text);
		});
	}

	public static void hide() {
		Platform.runLater(() -> stage.close());

	}

}
