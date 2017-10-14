package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.IInstallable;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class ProjectTile extends HBox implements ProjectInfo {

    @FXML
    protected ImageView imageView;
    @FXML
    protected Hyperlink title;
    @FXML
    protected JFXButton buttonInstall;
    @FXML
    protected StackPane nodePane;
    @FXML
    protected ComboBox<Database.ProjectFile> comboFile;
    @FXML
    protected VBox left, right;

    private Instance instance;
    private IInstallable installer;

    public ProjectTile(Instance instance, IInstallable installer) {
        this.instance = instance;
        this.installer = installer;
        FXMLLoader fxmlLoader = new FXMLLoader(FileUtil.getResource("gui/contentpanes/install_tile.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        right.setAlignment(Pos.BASELINE_RIGHT);
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
    }

    @Override
    public ComboBox<Database.ProjectFile> getCombo() {
        return comboFile;
    }

    @Override
    public Hyperlink getTitle() {
        return title;
    }

    @Override
    public VBox getLeft() {
        return left;
    }

    @Override
    public VBox getRight() {
        return right;
    }

    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public Button getInstallButton() {
        return buttonInstall;
    }
}
