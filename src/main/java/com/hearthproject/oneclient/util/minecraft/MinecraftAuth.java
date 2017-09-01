package com.hearthproject.oneclient.util.minecraft;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

public class MinecraftAuth {

	public static void loginAndPlay(Instance instance) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/mc_auth.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.logger.error("An error has occurred loading mc_auth.fxml!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent root = fxmlLoader.load(fxmlUrl.openStream());
			Stage stage = new Stage();
			stage.setTitle("One Client - Login to minecraft");
			stage.getIcons().add(new Image("images/icon.png"));
			stage.setResizable(false);
			stage.initOwner(Main.stage);
			stage.initModality(Modality.WINDOW_MODAL);
			Scene scene = new Scene(root, 600, 300);
			scene.getStylesheets().add("gui/css/theme.css");
			stage.setScene(scene);
			stage.show();
			MinecraftAuthController controller = fxmlLoader.getController();
			controller.stage = stage;
			controller.instance = instance;
			controller.buttonLogin.setDefaultButton(true);
			controller.load(instance != null);
		} catch (Exception e) {
			OneClientLogging.error(e);
		}
	}

}
