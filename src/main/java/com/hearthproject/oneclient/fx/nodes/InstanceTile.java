package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.fx.contentpane.InstancePane;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
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

public class InstanceTile extends StackPane implements ProjectInfo {
    public final GaussianBlur blurEffect = new GaussianBlur(0);
    public final Instance instance;
    @FXML
    public Text modpackText;
    @FXML
    public ImageView imageView;
    @FXML
    public Text statusText;
    @FXML
    public JFXButton playButton;
    @FXML
    public JFXButton editButton;
    @FXML
    public StackPane nodePane;

    public InstanceTile(Instance instance) {
        if (instance == null)
            throw new NullPointerException("Missing Instance!");
        this.instance = instance;
        URL loc = Thread.currentThread().getContextClassLoader().getResource("gui/contentpanes/instance_tile.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(loc);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setImage(instance.getIcon(), instance.getName());


        modpackText.setText(instance.getName());
        statusText.setText(instance.getGameVersion());
        statusText.setFill(Color.web("#FFFFFF"));
        nodePane.setOpacity(0F);
        playButton.setOnAction(event -> MinecraftUtil.startMinecraft(this.instance));
        editButton.setOnAction(event -> InstancePane.show(instance));
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

        playButton.visibleProperty().bind(nodePane.hoverProperty());
        editButton.visibleProperty().bind(nodePane.hoverProperty());

        imageView.setEffect(blurEffect);
        blurEffect.radiusProperty().bind(nodePane.opacityProperty().multiply(18));

        playButton.disableProperty().bind(instance.installingProperty());
        editButton.disableProperty().bind(instance.installingProperty());
        imageView.disableProperty().bind(instance.installingProperty());
        instance.installingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                statusText.setText("Installing...");
            } else {
                statusText.setText(instance.getGameVersion());
            }
        });
    }

    @Override
    public ComboBox<Database.ProjectFile> getCombo() {
        return null;
    }

    public void setTitle(String name) {
        setTitle(name, "");
    }

    @Override
    public void setTitle(String name, String url) {
        modpackText.setText(name);
    }

    @Override
    public Hyperlink getTitle() {
        return null;
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
        return null;
    }

    @Override
    public void setImage(String path, String name) {
        ImageUtil.ImageOpenService service = new ImageUtil.ImageOpenService(path, name);
        service.setOnSucceeded(event -> MiscUtil.runLaterIfNeeded(() -> getImageView().setImage(service.getValue())));
        service.start();
    }
}
