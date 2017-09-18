package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.DownloadManager;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.Mod;
import com.hearthproject.oneclient.api.curse.CurseMod;
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

public class ModTile extends StackPane {
	@FXML
	protected ImageView imageView;

	@FXML
	protected Hyperlink title;

	@FXML
	protected VBox left, middle, right;

	@FXML
	protected Button buttonInstall;

	protected Instance instance;
	protected Mod mod;

	protected ComboBox<CurseProject.CurseFile> comboFile;

	public ModTile(Instance instance, Mod mod) {
		this.instance = instance;
		this.mod = mod;

		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/install_tile.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(loc);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		title.setText(this.mod.getName());
		title.setTextFill(Color.web("#FFFFFF"));
		title.setFont(javafx.scene.text.Font.font(title.getFont().getFamily(), FontWeight.BOLD, title.getFont().getSize()));
		if (mod instanceof CurseMod) {
			title.setOnAction(event -> OperatingSystem.browseURI(((CurseMod) this.mod).data.WebSiteURL));
			comboFile = new ComboBox<>(FXCollections.observableArrayList(((CurseMod) mod).getFiles()));
			comboFile.getSelectionModel().selectFirst();
			right.getChildren().add(comboFile);
			buttonInstall.setOnAction(event ->
				DownloadManager.startDownload(instance.getName(), () -> {
					((CurseMod) mod).setFileData(comboFile.getValue().toFileData());
					mod.install(instance);
				})
			);

		}
		//
		//		if (this.mod.getInstaller() instanceof CurseInstaller) {
		//			comboFile = new ComboBox<>(FXCollections.observableArrayList(((CurseInstaller) this.mod.getInstaller()).getFiles()));
		//			comboFile.getSelectionModel().selectFirst();
		//			right.getChildren().add(comboFile);
		//			((CurseInstaller) this.mod.getInstaller()).setFile(comboFile.getValue());
		//			comboFile.valueProperty().addListener((v, a, b) -> ((CurseInstaller) this.mod.getInstaller()).setFile(b));
		//		}
		//		//		left.getChildren().addAll(((List<CurseProject.Category>)instance.info.get("categories")).stream().map(CurseProject.Category::getNode).collect(Collectors.toList()));
		//		middle.getChildren().addAll(info(getPopularity()), info(this.mod.info.get("authors")));

		//		buttonInstall.setOnAction(event -> DownloadManager.startDownload(this.mod.getName(), this.mod::install));
		//		imageView.setImage(this.mod.getImage());
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

	private String getName() {
		return mod.getName().trim();
	}

}
