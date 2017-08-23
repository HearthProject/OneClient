package com.hearthproject.oneclient.fx.contentpane.instanceView;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class InstancePane extends ContentPane {

	public Text textPackName;
	public Text textMinecraftVersion;
	public ImageView packIcon;
	public Button buttonPlay;
	public Button buttonOpenFolder;
	public ListView modList;

	public InstancePane() {
		super("gui/contentpanes/instanceView/instanceView.fxml", "InstancePane");
	}

	public static void show(Instance instance){
		InstancePane pane = (InstancePane) ContentPanes.getPane(InstancePane.class);
		pane.setupPane(instance);
		VBox.setVgrow(pane.getNode(), Priority.ALWAYS);
		HBox.setHgrow(pane.getNode(), Priority.ALWAYS);
		Main.mainController.setContent(pane);
	}

	public void setupPane(Instance instance){
		textPackName.setText(instance.name);
		textMinecraftVersion.setText("Minecraft " + instance.minecraftVersion);
		if(instance.getIcon() != null){
			try {
				packIcon = new ImageView(new Image(FileUtils.openInputStream(instance.getIcon())));
				packIcon.setFitWidth(150);
				packIcon.setFitHeight(150);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for(File mod : new File(instance.getDirectory(), "mods").listFiles()){
			modList.getItems().add(mod.getName());
		}
	}

	@Override
	protected void onStart() {

	}

	@Override
	public void refresh() {

	}
}
