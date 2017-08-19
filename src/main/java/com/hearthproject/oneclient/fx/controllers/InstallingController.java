package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class InstallingController {
	public ProgressBar progressbar;
	public Text title;
	public Text detail;
	public TextArea log;

	public static InstallingController controller;
	static Stage stage;

	public static void showInstaller() throws IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL fxmlUrl = classLoader.getResource("gui/install_screen.fxml");
		if (fxmlUrl == null) {
			OneClientLogging.log("An error has occurred loading newInstance.fxml!");
			return;
		}
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(fxmlUrl);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		Parent root = fxmlLoader.load(fxmlUrl.openStream());
		stage = new Stage();
		stage.setTitle("Installing");
		stage.getIcons().add(new Image("icon.png"));
		stage.setResizable(false);
		stage.initOwner(Main.stage);
		stage.initModality(Modality.WINDOW_MODAL);
		Scene scene = new Scene(root, 600, 500);
		scene.getStylesheets().add("gui/css/theme.css");
		stage.setScene(scene);
		stage.show();
		controller = fxmlLoader.getController();
	}

	public static void close(){
		Platform.runLater(() -> stage.close());
	}

	public void log(String text){
		if(Platform.isFxApplicationThread()){
			log.appendText(text + "\n");
		} else {
			Platform.runLater(() -> log.appendText(text + "\n"));
		}
	}

	public void setDetailText(String text){
		log(text);
		if(Platform.isFxApplicationThread()){
			detail.setText(text);
		} else {
			Platform.runLater(() -> detail.setText(text));
		}
	}

	public void setTitleText(String text){
		if(Platform.isFxApplicationThread()){
			title.setText(text);
		} else {
			Platform.runLater(() -> title.setText(text));
		}
	}

	public void setProgress(double progress){
		if(Platform.isFxApplicationThread()){
			progressbar.setProgress(progress / 100);
		} else {
			progressbar.setProgress(progress / 100);
		}
	}

	public void setProgress(int count, int maxCount){
		double percent = ((count * 100.0f) / maxCount) / 100;
		if(Platform.isFxApplicationThread()){
			progressbar.setProgress(percent);
		} else {
			progressbar.setProgress(percent);
		}
	}


}
