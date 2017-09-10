package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LogController {

	private static final int MAX_CHARS = 50000;

	public TabPane root;
	public TextArea logArea;

	public List<Process> processList = new ArrayList<>();
	private Stage stage;

	public LogTab getTab(String name, Process process) {
		LogTab tab = new LogTab(name, process);
		MiscUtil.runLaterIfNeeded(() -> root.getTabs().add(tab));
		return tab;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void menuUpload(ActionEvent actionEvent) {
		MiscUtil.uploadLog(logArea.getText());
	}

	public void menuClose(ActionEvent actionEvent) {
		stage.hide();
	}

	public void menuClear(ActionEvent actionEvent) { logArea.clear(); }

	public class LogTab extends Tab {
		@FXML
		private TextArea textArea;
		@FXML
		private MenuItem kill, upload, clear;

		private Process process;

		public LogTab(String name, Process process) {
			setText(name);

			this.process = process;
			URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/log_tab.fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(loc);
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			try {
				fxmlLoader.load();
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
			kill.setOnAction(event -> kill());
			clear.setOnAction(event -> textArea.clear());
			upload.setOnAction(event -> MiscUtil.uploadLog(textArea.getText()));
			textArea.setTextFormatter(new TextFormatter<String>(change ->
				change.getControlNewText().length() <= MAX_CHARS ? change : null));
		}

		public void kill() {
			OneClientLogging.logger.info("{} was forcefully terminated by the user!", this.getText());
			this.process.destroyForcibly();
		}

		public void append(String message) {
			try {
				if (textArea != null) {
					MiscUtil.runLaterIfNeeded(() -> {
						if (textArea.getText().length() == 0) {
							textArea.setText(message);
						} else {
							textArea.selectEnd();
							textArea.insertText(textArea.getText().length(),
								message);
						}
					});
				}
			} catch (Throwable e) {
				OneClientLogging.error(e);
			}

		}
	}

}
