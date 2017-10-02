package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.fx.contentpane.InstancePane;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class InstanceTile extends StackPane {
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

		new Thread(() -> MiscUtil.runLaterIfNeeded(() -> imageView.setImage(ImageUtil.openCachedImage(instance.getIcon(), instance.getName())))).start();

		imageView.setEffect(blurEffect);
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

}
