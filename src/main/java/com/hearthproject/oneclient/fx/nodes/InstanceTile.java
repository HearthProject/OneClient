package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.fx.contentpane.instance.InstancePane;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class InstanceTile extends StackPane {
	public final Instance instance;
	public Rectangle background;
	public ImageView imageView;
	public VBox nodeBox;
	public Text nameLabel;
	public Text statusLabel;
	public Button playButton;
	public Button editButton;
	private Action action;

	public InstanceTile(Instance instance) {
		this.instance = instance;
		background = new Rectangle(192, 192);
		background.setArcHeight(0);
		background.setArcWidth(0);
		background.setFill(Color.web("#262626"));
		background.setStrokeWidth(0);
		imageView = new ImageView();
		imageView.setImage(ImageUtil.openImage(instance.getManifest().getIcon()));
		imageView.setFitHeight(75);
		imageView.setFitWidth(75);
		nameLabel = new Text(instance.getManifest().getName());
		nameLabel.setFill(Color.web("#FFFFFF"));
		nameLabel.setFont(javafx.scene.text.Font.font(nameLabel.getFont().getFamily(), FontWeight.BOLD, nameLabel.getFont().getSize()));
		statusLabel = new Text(instance.getManifest().getMinecraftVersion());
		statusLabel.setFill(Color.web("#FFFFFF"));
		playButton = new Button("Play");
		playButton.setOnAction(event -> {
			if (action != null)
				action.execute();
		});
		editButton = new Button("Edit");
		editButton.setOnAction(event -> InstancePane.show(instance));
		nodeBox = new VBox(nameLabel, statusLabel, imageView, playButton, editButton);
		nodeBox.setAlignment(Pos.CENTER);
		nodeBox.setSpacing(6);
		this.getChildren().addAll(background, nodeBox);
		this.setAlignment(Pos.CENTER);
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public interface Action {
		void execute();
	}

	public void setInstalling(boolean installing) {
		MiscUtil.runLaterIfNeeded(() -> {
			playButton.setDisable(installing);
			editButton.setDisable(installing);
			if (installing) {
				statusLabel.setText("Installing...");
			} else {
				statusLabel.setText(instance.getManifest().getMinecraftVersion());
			}
		});
	}

}
