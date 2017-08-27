package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class CurseTile extends StackPane {
	@FXML
	protected ImageView imageView;

	@FXML
	protected Hyperlink title;

	@FXML
	protected VBox info, authors;

	@FXML
	protected Button buttonInstall;

	protected final CurseElement element;

	public CurseTile(CurseElement element) {
		this.element = element;
		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/curse_tile.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(loc);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		title.setText(element.getTitle());
		title.setTextFill(Color.web("#FFFFFF"));
		title.setFont(javafx.scene.text.Font.font(title.getFont().getFamily(), FontWeight.BOLD, title.getFont().getSize()));
		title.setOnAction(event -> OperatingSystem.browseURI(element.getUrl()));

		Label average = new Label(element.getAverageDownloads());
		Label total = new Label(element.getTotalDownloads());
		Label date = new Label(element.getCreatedDate());
		Label updated = new Label(element.getLastUpdated());
		Label version = new Label(element.getVersion());
		average.setTextFill(Color.web("#FFFFFF"));
		total.setTextFill(Color.web("#FFFFFF"));
		date.setTextFill(Color.web("#FFFFFF"));
		updated.setTextFill(Color.web("#FFFFFF"));
		version.setTextFill(Color.web("#FFFFFF"));

		info.getChildren().addAll(average, total, updated, date, version);
		element.getAuthors().stream().map(a -> {
			Label author = new Label(a);
			author.setTextFill(Color.web("#FFFFFF"));
			return author;
		}).forEach(authors.getChildren()::add);

		buttonInstall.setOnAction(event -> install());
		getImageFile();
	}

	public abstract void install();

	public File getImageFile() {
		File imageFile = null;
		String icon = element.getIcon();
		if (icon != null && !icon.isEmpty()) {
			Image image = CurseUtils.IMAGE_CACHE.getIfPresent(element.getTitle().replaceAll(":", ""));
			if (image == null) {
				File dir = new File(Constants.TEMPDIR, "icons");
				if (!dir.exists())
					dir.mkdir();
				imageFile = new File(dir, element.getTitle().replaceAll(":", "") + ".jpeg");
				if (!imageFile.exists()) {
					try (InputStream in = new URL(element.getIcon()).openStream()) {
						Files.copy(in, Paths.get(dir.toString(), element.getTitle().replaceAll(":", "") + ".jpeg"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (imageFile.exists()) {
					try {
						image = new Image(new FileInputStream(imageFile));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			if (image != null) {
				CurseUtils.IMAGE_CACHE.put(element.getTitle(), image);
				imageView.setImage(image);
			}
			imageView.setFitHeight(75);
			imageView.setFitWidth(75);
		}
		return imageFile;
	}

}
