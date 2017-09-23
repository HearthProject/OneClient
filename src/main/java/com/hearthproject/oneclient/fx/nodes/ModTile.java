package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.curse.CurseModInstaller;
import com.hearthproject.oneclient.api.curse.data.CurseFullProject;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class ModTile extends HBox implements Comparable<ModTile> {
	@FXML
	protected ImageView imageView;
	@FXML
	protected Hyperlink title;
	@FXML
	protected JFXButton buttonInstall;
	@FXML
	protected StackPane nodePane;
	@FXML
	protected ComboBox<CurseFullProject.CurseFile> comboFile;
	@FXML
	protected VBox left, right;

	protected Instance instance;
	protected ModInstaller mod;

	public ModTile(Instance instance, CurseModInstaller mod) {
		this.instance = instance;
		this.mod = mod;

		FXMLLoader fxmlLoader = new FXMLLoader(FileUtil.getResource("gui/contentpanes/install_tile.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		ImageUtil.ImageService service = new ImageUtil.ImageService(mod.project.getIcon(), mod.getName());
		service.setOnSucceeded(event -> MiscUtil.runLaterIfNeeded(() -> imageView.setImage(service.getValue())));
		service.start();

		MiscUtil.setupLink(title, mod.getName(), mod.project.getWebSiteURL());
		comboFile.setVisible(true);
		comboFile.setItems(FXCollections.observableArrayList(mod.getFiles()));
		comboFile.getSelectionModel().selectFirst();
		if (!comboFile.getItems().isEmpty()) {
			mod.setFile(comboFile.getValue());
			comboFile.valueProperty().addListener((v, a, b) -> mod.setFile(comboFile.getValue()));
		}

		Label downloads = info("Downloads: %s", MiscUtil.formatNumbers(mod.project.getDownloads()));
		Label gameVersions = info("Versions: %s", mod.project.getVersions());
		right.setAlignment(Pos.BASELINE_RIGHT);
		right.getChildren().addAll(gameVersions, downloads);
		left.getChildren().addAll(info("By %s", mod.project.getAuthorsString()));

		DownloadTask task = DownloadManager.createDownload(instance.getName(), () -> mod.install(instance));
		buttonInstall.setOnAction(event -> {
			task.start();
			buttonInstall.setDisable(true);
		});
		task.setOnSucceeded(event -> {
			buttonInstall.setDisable(false);
			instance.verifyMods();
		});

		nodePane.setOpacity(0F);
		nodePane.hoverProperty().addListener((observable, oldValue, newValue) -> {
			FadeTransition fadeTransition = new FadeTransition(new Duration(400), nodePane);
			if (newValue) {
				fadeTransition.setFromValue(0F);
				fadeTransition.setToValue(1F);
				fadeTransition.play();
				nodePane.setOpacity(1F);
			} else {
				fadeTransition.setFromValue(1F);
				fadeTransition.setToValue(0F);
				fadeTransition.play();
				nodePane.setOpacity(0F);
			}
		});
		imageView.disableProperty().bind(instance.installingProperty());
	}

	public Label info(String format, Object... params) {
		return info(String.format(format, params));
	}

	public Label info(String value) {
		Label l = new Label(value);
		l.setId("oc-info-label");
		return l;
	}

	private String getName() {
		return instance.name.trim();
	}

	@Override
	public int compareTo(ModTile o) {
		return getName().compareTo(o.getName());
	}
}
