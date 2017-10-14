package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModpackInstaller;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.DownloadManager;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.api.modpack.curse.CurseDownloader;
import com.hearthproject.oneclient.api.modpack.curse.CurseFileData;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class FeaturedTile extends StackPane implements ProjectInfo {
    public final GaussianBlur blurEffect = new GaussianBlur(0);

    public final Instance instance;

    @FXML
    public Hyperlink modpackText;
    @FXML
    public ImageView imageView;
    @FXML
    public Text statusText;
    @FXML
    public JFXButton installButton;
    @FXML
    public StackPane nodePane;
    @FXML
    public ComboBox<Database.ProjectFile> files;

    public FeaturedTile(Instance instance, ModpackInstaller installer) {
        this.instance = instance;
        URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/featured_tile.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(loc);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        statusText.setText(instance.getGameVersion());
        statusText.setFill(Color.web("#FFFFFF"));
        nodePane.setOpacity(0F);

        if (installer instanceof CurseDownloader) {
            CurseDownloader i = (CurseDownloader) installer;
            CurseFileData data = i.getData();
            Database.Project project = data.getProject();
            setImage(project.getIconURL(), instance.getName());
            setTitle(instance.getName(), Curse.getCurseForge(project.getId()));
            setFiles(project.getProjectFiles());
            data.setProjectFile(getCombo().getValue());
            files.valueProperty().addListener((v, a, b) -> data.setProjectFile(getCombo().getValue()));
        }

        DownloadTask task = DownloadManager.createDownload(instance.getName(), instance::install);
        installButton.setOnAction(event -> task.start());
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
        installButton.disableProperty().bind(instance.installingProperty());
        imageView.disableProperty().bind(instance.installingProperty());
        instance.installingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                statusText.setText("Installing...");
            } else {
                statusText.setText(instance.getGameVersion());
            }
        });

        imageView.setEffect(blurEffect);
        blurEffect.radiusProperty().bind(nodePane.opacityProperty().multiply(18));
    }

    @Override
    public ComboBox<Database.ProjectFile> getCombo() {
        return files;
    }

    @Override
    public Hyperlink getTitle() {
        return modpackText;
    }

    @Override
    public VBox getLeft() {
        return null;
    }

    @Override
    public VBox getRight() {
        return null;
    }

    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public Button getInstallButton() {
        return installButton;
    }
}
