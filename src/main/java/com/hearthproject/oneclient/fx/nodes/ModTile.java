package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.ModInstaller;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class ModTile extends HBox implements Comparable<ModTile> {
    public final GaussianBlur blurEffect = new GaussianBlur(0);

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

    protected Instance instance;
    protected ModInstaller mod;

    public ModTile(Instance instance, ModInstaller mod) {
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
        MiscUtil.setupLink(title, mod.getName(), "");
        imageView.setEffect(blurEffect);
        blurEffect.radiusProperty().bind(nodePane.opacityProperty().multiply(10));
        buttonInstall.setVisible(false);
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


    @Override
    public int compareTo(ModTile o) {
        return mod.getName().compareTo(o.mod.getName());
    }

}
