package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.fx.contentpane.CursePacksPane;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.curse.CursePack;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CurseTile extends StackPane {

	public final CursePack pack;
	public ImageView imageView;
	public HBox nodeBox;
	public Hyperlink labelTitle;
	public Button buttonInstall;

	public CurseTile(CursePacksPane parent, CursePack pack) {
		this.pack = pack;
		imageView = new ImageView();
		labelTitle = new Hyperlink(pack.getTitle());
		labelTitle.setTextFill(Color.web("#FFFFFF"));
		labelTitle.setFont(javafx.scene.text.Font.font(labelTitle.getFont().getFamily(), FontWeight.BOLD, labelTitle.getFont().getSize()));
		labelTitle.setOnAction(event -> OperatingSystem.browseURI(pack.getUrl()));
		buttonInstall = new Button("Install");
		final File imageFile = getImageFile();
		buttonInstall.setOnAction(event -> parent.install(instance -> {
			new CursePackInstaller().downloadFromURL(pack.getUrl(), "latest", instance);
			if (imageFile != null)
				Files.copy(imageFile.toPath(), new File(instance.getDirectory(), "icon.png").toPath());
		}));
		Label average = new Label(pack.getAverageDownloads());
		Label total = new Label(pack.getTotalDownloads());
		average.setTextFill(Color.web("#FFFFFF"));
		total.setTextFill(Color.web("#FFFFFF"));
		Label date = new Label(pack.getCreatedDate());
		date.setTextFill(Color.web("#FFFFFF"));
		Label version = new Label(pack.getVersion());
		version.setTextFill(Color.web("#FFFFFF"));

		VBox vBox = new VBox(labelTitle, average, total, date, version);
		nodeBox = new HBox(buttonInstall, imageView, vBox);
		nodeBox.setAlignment(Pos.CENTER_LEFT);
		nodeBox.setSpacing(6);
		this.getChildren().addAll(nodeBox);
		this.setAlignment(Pos.CENTER_LEFT);
	}

	public File getImageFile() {
		File imageFile = null;
		String icon = pack.getIcon();
		if (icon != null && !icon.isEmpty()) {
			Image image = null;
			if (CurseUtils.IMAGE_CACHE.containsKey(pack.getTitle()))
				image = CurseUtils.IMAGE_CACHE.get(pack.getTitle());
			if (image == null) {
				File dir = new File(Constants.TEMPDIR, "icons");
				if (!dir.exists())
					dir.mkdir();
				imageFile = new File(dir, pack.getTitle() + ".jpeg");
				if (!imageFile.exists()) {
					try (InputStream in = new URL(pack.getIcon()).openStream()) {
						Files.copy(in, Paths.get(dir.toString(), pack.getTitle() + ".jpeg"));
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
