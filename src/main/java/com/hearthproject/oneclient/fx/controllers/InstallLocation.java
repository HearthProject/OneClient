package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Predicate;

public class InstallLocation {
	public TextField locationField;

	public Button browseButton;
	public Button okButton;

	public static void getInstallDir(Predicate<File> predicate) throws IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL fxmlUrl = classLoader.getResource("gui/install_location.fxml");
		if (fxmlUrl == null) {
			OneClientLogging.logger.error("An error has occurred loading the fxml!");
		}
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(fxmlUrl);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		Parent root = fxmlLoader.load(fxmlUrl.openStream());
		Stage stage = new Stage();
		stage.setTitle("Select Install Location");
		stage.getIcons().add(new Image("images/icon.png"));
		stage.setResizable(false);
		stage.initOwner(Main.stage);
		stage.initModality(Modality.WINDOW_MODAL);
		Scene scene = new Scene(root, 500, 150);
		scene.getStylesheets().add("gui/css/theme.css");
		stage.setScene(scene);
		stage.show();
		InstallLocation controller = fxmlLoader.getController();
		controller.okButton.setOnAction(event -> {
			predicate.test(new File(controller.locationField.getText()));
			stage.close();
		});
		controller.browseButton.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File(controller.locationField.getText()));
			File selectedDirectory = directoryChooser.showDialog(stage);
			if (selectedDirectory != null) {
				controller.locationField.setText(selectedDirectory.getAbsolutePath());
			}
		});
		controller.locationField.setText(Constants.getDefaultDir().getAbsolutePath());
	}

}
