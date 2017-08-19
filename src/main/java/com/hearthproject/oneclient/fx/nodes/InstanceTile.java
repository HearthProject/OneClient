package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.json.models.launcher.Instance;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class InstanceTile extends StackPane {
	public final Instance instance;
	public Rectangle background;
	public ImageView imageView;
	public VBox nodeBox;
	public Text nameLabel;
	public Button playButton;
	private Action action;

	public InstanceTile(Instance instance) {
		this.instance = instance;
		background = new Rectangle(192, 192);
		background.setArcHeight(0);
		background.setArcWidth(0);
		background.setFill(Color.web("#262626"));
		background.setStrokeWidth(0);
		imageView = new ImageView();
		if (!instance.icon.isEmpty()){
			try {
				imageView.setImage(new Image(new FileInputStream(instance.getIcon())));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			imageView.setFitHeight(75);
			imageView.setFitWidth(75);
		}
		nameLabel = new Text(instance.name);
		nameLabel.setFill(Color.web("#FFFFFF"));
		nameLabel.setFont(javafx.scene.text.Font.font(nameLabel.getFont().getFamily(), FontWeight.BOLD, nameLabel.getFont().getSize()));
		playButton = new Button("Play");
		playButton.setOnAction(event -> {
			if (action != null)
				action.execute();
		});
		nodeBox = new VBox(nameLabel, playButton);
		nodeBox.setAlignment(Pos.CENTER);
		nodeBox.setSpacing(6);
		this.getChildren().addAll(background, imageView, nodeBox);
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
}
