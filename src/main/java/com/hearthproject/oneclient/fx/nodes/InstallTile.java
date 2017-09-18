package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.curse.CurseInstaller;
import com.hearthproject.oneclient.api.curse.data.CurseProject;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;

public class InstallTile extends StackPane implements Comparable<InstallTile> {
	@FXML
	protected ImageView imageView;

	@FXML
	protected Hyperlink title;

	@FXML
	protected VBox left, middle, right;

	@FXML
	protected Button buttonInstall;

	protected Instance instance;

	protected ComboBox<CurseProject.CurseFile> comboFile;

	public InstallTile(Instance instance) {
		this.instance = instance;

		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/install_tile.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(loc);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		title.setText(instance.getName());
		title.setTextFill(Color.web("#FFFFFF"));
		title.setFont(javafx.scene.text.Font.font(title.getFont().getFamily(), FontWeight.BOLD, title.getFont().getSize()));
		title.setOnAction(event -> OperatingSystem.browseURI(instance.info.get("websiteUrl").toString()));

		if (instance.getInstaller() instanceof CurseInstaller) {
			comboFile = new ComboBox<>(FXCollections.observableArrayList(((CurseInstaller) instance.getInstaller()).getFiles()));
			comboFile.getSelectionModel().selectFirst();
			right.getChildren().add(comboFile);
			((CurseInstaller) instance.getInstaller()).setFile(comboFile.getValue());
			comboFile.valueProperty().addListener((v, a, b) -> ((CurseInstaller) instance.getInstaller()).setFile(b));
		}
		//		left.getChildren().addAll(((List<CurseProject.Category>)instance.info.get("categories")).stream().map(CurseProject.Category::getNode).collect(Collectors.toList()));
		middle.getChildren().addAll(info(getPopularity()), info(instance.info.get("authors")));

		DownloadTask task = DownloadManager.createDownload(instance.getName(), instance::install);
		buttonInstall.disableProperty().bind(task.runningProperty());
		buttonInstall.setOnAction(event -> task.start());

		imageView.setImage(instance.getImage());
		imageView.setFitHeight(75);
		imageView.setFitWidth(75);
	}

	public Label info(Object value) {
		return info(value.toString());
	}

	public Label info(String value) {
		Label l = new Label(value);
		l.setTextFill(Color.web("#FFFFFF"));
		return l;
	}

	public double getPopularity() {
		return (double) instance.info.get("popularity");
	}

	private String getName() {
		return instance.name.trim();
	}

	@Override
	public int compareTo(InstallTile o) {
		return getName().compareTo(o.getName());
	}
}
