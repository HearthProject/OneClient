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
import java.util.Map;

public class CurseTile extends StackPane {

    public final CursePack pack;
    public ImageView imageView;
    public VBox nodeBox;
    public Hyperlink nameLabel;
    public Button buttonInstall;

    public CurseTile(CursePacksPane parent, CursePack pack) {
        this.pack = pack;

        imageView = new ImageView();
        nameLabel = new Hyperlink(pack.getTitle());
        nameLabel.setTextFill(Color.web("#FFFFFF"));
        nameLabel.setFont(javafx.scene.text.Font.font(nameLabel.getFont().getFamily(), FontWeight.BOLD, nameLabel.getFont().getSize()));
        nameLabel.setOnAction(event -> OperatingSystem.browseURI(pack.getUrl()));
        buttonInstall = new Button("Install");
        final File imageFile = getImageFile();
        buttonInstall.setOnAction(event -> parent.install(instance -> {
            new CursePackInstaller().downloadFromURL(pack.getUrl(), "latest", instance);
            if(imageFile != null)
                Files.copy(imageFile.toPath(), new File(instance.getDirectory(), "icon.png").toPath());
        }));
        nodeBox = new VBox(nameLabel, imageView, buttonInstall);
        nodeBox.setAlignment(Pos.CENTER);
        nodeBox.setSpacing(6);
        nodeBox.setId("#dark-background");

        this.getChildren().addAll( nodeBox);
        this.setAlignment(Pos.CENTER);
    }

    public File getImageFile() {
        File imageFile = null;
        if (!pack.getIcon().isEmpty()) {
            Image image = null;
            Map map = CurseUtils.IMAGE_CACHE;
            if (CurseUtils.IMAGE_CACHE.containsKey(pack.getTitle()))
                image = CurseUtils.IMAGE_CACHE.get(pack.getTitle());
            if (image == null) {
                File dir = new File(Constants.TEMPDIR, "icons");
                if (!dir.exists())
                    dir.mkdir();
                imageFile = new File(dir, pack.getTitle() + ".png");
                if (!imageFile.exists()) {
                    try (InputStream in = new URL(pack.getIcon()).openStream()) {
                        Files.copy(in, Paths.get(dir.toString(), pack.getTitle() + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(imageFile.exists()) {
                    try {
                        image = new Image(new FileInputStream(imageFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(image != null) {
                CurseUtils.IMAGE_CACHE.put(pack.getTitle(),image);
                imageView.setImage(image);
            }
            imageView.setFitHeight(75);
            imageView.setFitWidth(75);
        }
        return imageFile;
    }

}
