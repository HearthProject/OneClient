package com.hearthproject.oneclient.fxnodes;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


public class InstanceTile extends StackPane {
    public Rectangle background;
    public ImageView imageView;
    public VBox nodeBox;
    public Text nameLabel;
    public Button playButton;

    public InstanceTile() {
        background = new Rectangle(192, 192);
        background.setArcHeight(0);
        background.setArcWidth(0);
        background.setFill(Color.web("#262626"));
        background.setStrokeWidth(0);
        getChildren().addAll(background, imageView, nodeBox);
    }
}
