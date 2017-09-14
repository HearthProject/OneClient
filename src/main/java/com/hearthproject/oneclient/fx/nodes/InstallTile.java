package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;

public class InstallTile extends StackPane {
	@FXML
	protected ImageView imageView;

	@FXML
	protected Hyperlink title;

	@FXML
	protected VBox info, authors;

	@FXML
	protected Button buttonInstall;

	protected Instance instance;

	public InstallTile(Instance instance) {
		this.instance = instance;

		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/curse_tile.fxml");
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
		title.setOnAction(event -> OperatingSystem.browseURI(instance.getUrl()));

		//		Label average = new Label(element.getAverageDownloads());
		//		Label total = new Label(element.getTotalDownloads());
		//		Label date = new Label(element.getCreatedDate());
		//		Label updated = new Label(element.getLastUpdated());
		//		Label version = new Label(element.getVersion());
		//		average.setTextFill(Color.web("#FFFFFF"));
		//		total.setTextFill(Color.web("#FFFFFF"));
		//		date.setTextFill(Color.web("#FFFFFF"));
		//		updated.setTextFill(Color.web("#FFFFFF"));
		//		version.setTextFill(Color.web("#FFFFFF"));
		//
		//		info.getChildren().addAll(average, total, updated, date, version);
		//		element.getAuthors().stream().map(a -> {
		//			Label author = new Label(a);
		//			author.setTextFill(Color.web("#FFFFFF"));
		//			return author;
		//		}).forEach(authors.getChildren()::add);

		buttonInstall.setOnAction(event -> instance.install());
		imageView.setImage(instance.getImage());
		imageView.setFitHeight(75);
		imageView.setFitWidth(75);
	}


}
