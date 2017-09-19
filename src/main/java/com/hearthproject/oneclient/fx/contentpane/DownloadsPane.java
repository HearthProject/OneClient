package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.skins.TaskSkin;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.TaskProgressView;

import java.util.Comparator;

public class DownloadsPane extends ContentPane {

	@FXML
	public TaskProgressView<DownloadTask> downloads;
	@FXML
	public VBox root;
	public Label placeholder;
	public DownloadsPane() {
		super("gui/contentpanes/downloads.fxml", "Downloads", "download.png", ButtonDisplay.ABOVE_DIVIDER);
	}

	@Override
	public void onStart() {
		root.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		root.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		Bindings.bindContent(downloads.getTasks(), FXCollections.observableArrayList(DownloadManager.DOWNLOADS.values()));
		placeholder = new Label("No Downloads");
		downloads.getTasks().sort(Comparator.comparing(Task::isRunning));
		downloads.setSkin(new TaskSkin<>(downloads));
	}

	@Override
	public void refresh() {

	}

	public void flashButton() {
		button.setSelected(true);
		PauseTransition delay = new PauseTransition(Duration.seconds(1));
		delay.setOnFinished(e -> button.setSelected(false));
		delay.play();
	}
}
