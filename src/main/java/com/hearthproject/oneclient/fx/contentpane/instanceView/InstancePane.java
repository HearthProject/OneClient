package com.hearthproject.oneclient.fx.contentpane.instanceView;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.minecraft.MinecraftAuth;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class InstancePane extends ContentPane {

	public Text textPackName;
	public Text textMinecraftVersion;
	public ImageView packIcon;
	public Button buttonPlay;
	public Button buttonOpenFolder;
	public ListView modList;

	public MenuItem menuOpenFolder;
	public MenuItem menuDelete;
	public MenuItem menuDownload;
	public MenuItem menuBackup;
	public MenuItem menuViewBackups;

	public Instance instance;

	public InstancePane() {
		super("gui/contentpanes/instanceView/instanceView.fxml", "InstancePane");
	}

	public static void show(Instance instance){
		InstancePane pane = (InstancePane) ContentPanes.getPane(InstancePane.class);
		pane.setupPane(instance);
		AnchorPane node = (AnchorPane) pane.getNode();
		VBox.setVgrow(node, Priority.ALWAYS);
		HBox.setHgrow(node, Priority.ALWAYS);
		Main.mainController.currentContent.button.setSelected(false);
		Main.mainController.setContent(pane);
	}

	public void setupPane(Instance instance){
		this.instance = instance;
		textPackName.setText(instance.name);
		textMinecraftVersion.setText("Minecraft " + instance.minecraftVersion);
		if(instance.getIcon() != null){
			try {
				packIcon.setImage(new Image(FileUtils.openInputStream(instance.getIcon())));
				packIcon.setFitWidth(150);
				packIcon.setFitHeight(150);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File modsDir = new File(instance.getDirectory(), "mods");
		if(modsDir.exists()){
			for(File mod : new File(instance.getDirectory(), "mods").listFiles()){
				modList.getItems().add(mod.getName());
			}

		}

		menuOpenFolder.setOnAction(event -> {
			try {
				Desktop.getDesktop().open(instance.getDirectory());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		buttonOpenFolder.setOnAction(event -> {
			try {
				Desktop.getDesktop().open(instance.getDirectory());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		buttonPlay.setOnAction(event -> MinecraftAuth.loginAndPlay(instance));

		menuDelete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Are you sure?");
				alert.setHeaderText("Are you sure you want to delete the instance");
				alert.setContentText("This will remove all mods and worlds, this cannot be undone!");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
					try {
						FileUtils.deleteDirectory(instance.getDirectory());
						ContentPanes.INSTANCES_PANE.button.fire();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	protected void onStart() {

	}

	@Override
	public void refresh() {

	}
}
