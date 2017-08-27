package com.hearthproject.oneclient.util.logging;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.controllers.LogController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class OneClientLogging {

	public static Stage stage;
	public static LogController logController;

	public static Logger logger = LogManager.getLogger("OneClientLogging");

	public static void init() {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		context.reconfigure();
	}

	public static void logUserError(Throwable throwable, String title) {
		StringWriter errors = new StringWriter();
		throwable.printStackTrace(new PrintWriter(errors));
		logger.error(errors.toString());
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error!");
			alert.setHeaderText(title);
			alert.setContentText(throwable.getLocalizedMessage());
			alert.showAndWait();
		});
	}

	public static void setupLogController() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/log.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.logger.error("An error has occurred loading newInstance.fxml!");
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
			TextAreaAppender.setTextArea(logController.logArea);
		} catch (Exception e) {
			OneClientLogging.logger.error(e);
		}
	}

	public static void showLogWindow() {
		stage.show();
	}

	public static void hideLogWindow() {
		stage.hide();
	}

}
