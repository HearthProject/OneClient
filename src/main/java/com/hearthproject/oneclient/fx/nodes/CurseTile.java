package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.contentpane.CursePacksPane;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.curse.CursePack;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
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

public class CurseTile extends StackPane {
	@FXML
	protected ImageView imageView;

	@FXML
	protected Hyperlink title;

	@FXML
	protected VBox info, authors;

	@FXML
	protected Button buttonInstall;

	protected final CursePack pack;
	protected final CursePacksPane parent;

	public CurseTile(CursePacksPane parent, CursePack pack) {
		this.pack = pack;
		this.parent = parent;
		URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/curse_tile.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(loc);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		title.setText(pack.getTitle());
		title.setTextFill(Color.web("#FFFFFF"));
		title.setFont(javafx.scene.text.Font.font(title.getFont().getFamily(), FontWeight.BOLD, title.getFont().getSize()));
		title.setOnAction(event -> OperatingSystem.browseURI(pack.getUrl()));
		final File imageFile = getImageFile();
		buttonInstall.setOnAction(event -> parent.install(instance -> {
			new CursePackInstaller().downloadFromURL(pack.getUrl(), "latest", instance);
			if (imageFile != null)
				Files.copy(imageFile.toPath(), new File(instance.getDirectory(), "icon.png").toPath());
		}));

		Label average = new Label(pack.getAverageDownloads());
		Label total = new Label(pack.getTotalDownloads());
		Label date = new Label(pack.getCreatedDate());
		Label updated = new Label(pack.getLastUpdated());
		Label version = new Label(pack.getVersion());
		average.setTextFill(Color.web("#FFFFFF"));
		total.setTextFill(Color.web("#FFFFFF"));
		date.setTextFill(Color.web("#FFFFFF"));
		updated.setTextFill(Color.web("#FFFFFF"));
		version.setTextFill(Color.web("#FFFFFF"));

		info.getChildren().addAll(average, total, updated, date, version);
		pack.getAuthors().stream().map(a -> {
			Label author = new Label(a);
			author.setTextFill(Color.web("#FFFFFF"));
			return author;
		}).forEach(authors.getChildren()::add);
	}

	public File getImageFile() {
		File imageFile = null;
		String icon = pack.getIcon();
		if (icon != null && !icon.isEmpty()) {
			Image image = CurseUtils.IMAGE_CACHE.getIfPresent(pack.getTitle().replaceAll(":", ""));
			if (image == null) {
				File dir = new File(Constants.TEMPDIR, "icons");
				if (!dir.exists())
					dir.mkdir();
				imageFile = new File(dir, pack.getTitle().replaceAll(":", "") + ".jpeg");
				if (!imageFile.exists()) {
					try (InputStream in = new URL(pack.getIcon()).openStream()) {
						Files.copy(in, Paths.get(dir.toString(), pack.getTitle().replaceAll(":", "") + ".jpeg"));
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
				CurseUtils.IMAGE_CACHE.put(pack.getTitle(), image);
				imageView.setImage(image);
			}
			imageView.setFitHeight(75);
			imageView.setFitWidth(75);
		}
		return imageFile;
	}

}
