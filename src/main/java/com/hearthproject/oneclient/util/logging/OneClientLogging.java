package com.hearthproject.oneclient.util.logging;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.LogController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OneClientLogging {

	public static Stage stage;
	static LogController logController;

	public static void log(String string) {
		String output = getPrefix() + string;
		System.out.println(output);
		if(SplashScreen.loaded){
			Platform.runLater(() -> logController.logArea.appendText(output + "\n"));
		}
		try {
			FileUtils.writeStringToFile(Constants.LOGFILE, output + "\n", StandardCharsets.UTF_8, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(Throwable throwable) {
		StringWriter errors = new StringWriter();
		throwable.printStackTrace(new PrintWriter(errors));
		log(errors.toString());
	}

	private static String getPrefix() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss.SSS");
		Date date = new Date();
		return "[" + dateFormat.format(date) + "] ";
	}

	public static void setupLogController() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/log.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.log("An error has occurred loading newInstance.fxml!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent root = fxmlLoader.load(fxmlUrl.openStream());
			stage = new Stage();
			stage.setTitle("One Client - Log");
			stage.getIcons().add(new Image("icon.png"));
			stage.setResizable(true);
			stage.initOwner(Main.stage);
			stage.initModality(Modality.WINDOW_MODAL);
			Scene scene = new Scene(root, 600, 300);
			scene.getStylesheets().add("gui/css/theme.css");
			stage.setScene(scene);
			logController = fxmlLoader.getController();
			logController.setStage(stage);
		} catch (Exception e) {
			OneClientLogging.log(e);
		}
	}

	public static void showLogWindow() {
		stage.show();
	}

}
