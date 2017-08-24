package com.hearthproject.oneclient.fx.contentpane.instanceView;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftAuth;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
	public Button buttonEditVersion;
	public Button buttonGetCurseMods;
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

	public static void show(Instance instance) {
		InstancePane pane = (InstancePane) ContentPanes.getPane(InstancePane.class);
		pane.setupPane(instance);
		AnchorPane node = (AnchorPane) pane.getNode();
		node.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		node.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		VBox.setVgrow(node, Priority.ALWAYS);
		HBox.setHgrow(node, Priority.ALWAYS);
		Main.mainController.currentContent.button.setSelected(false);
		Main.mainController.setContent(pane);
	}

	public void setupPane(Instance instance) {
		this.instance = instance;
		textPackName.setText(instance.name);
		textMinecraftVersion.setText("Minecraft " + instance.minecraftVersion);
		if (instance.getIcon() != null && instance.getIcon().exists()) {
			try {
				packIcon.setImage(new Image(FileUtils.openInputStream(instance.getIcon())));
				packIcon.setFitWidth(150);
				packIcon.setFitHeight(150);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		updateList();



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

		menuDelete.setOnAction(event -> {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Are you sure?");
			alert.setHeaderText("Are you sure you want to delete the instance");
			alert.setContentText("This will remove all mods and worlds, this cannot be undone!");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				try {
					FileUtils.deleteDirectory(instance.getDirectory());
					ContentPanes.INSTANCES_PANE.button.fire();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		buttonEditVersion.setOnAction(event -> NewInstanceController.start(instance));

		menuDownload.setDisable(true);
		menuBackup.setDisable(true);
		menuViewBackups.setDisable(true);
		buttonGetCurseMods.setDisable(true);

	}

	public void updateList(){
		modList.getItems().clear();
		File modsDir = new File(instance.getDirectory(), "mods");
		if (modsDir.exists()) {
			for (File mod : modsDir.listFiles()) {
				modList.getItems().add(mod.getName());
			}
		}

		modList.setCellFactory(param -> {
			ModListCell cell = new ModListCell();
			ContextMenu contextMenu = new ContextMenu();
			MenuItem disableMod = new MenuItem("Disable/Enable mod");
			disableMod.setOnAction(event -> {
				try {
					String item = cell.getItem();
					File mod = new File(modsDir, item);
					if(mod.exists()){
						if(item.endsWith(".disabled")){
							FileUtils.moveFile(mod, new File(mod.getParent(), mod.getName().substring(0, mod.getName().length() - ".disabled".length())));
						} else {
							FileUtils.moveFile(mod, new File(mod.getParent(), mod.getName() + ".disabled"));
						}
						updateList();
					} else {
						disableMod.setDisable(true);
					}
				} catch (IOException e){
					OneClientLogging.log(e);
				}
			});
			MenuItem deleteMod = new MenuItem("Delete Mod");
			deleteMod.setOnAction(event -> {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Are you sure?");
				alert.setHeaderText("Are you sure you want to delete this mod");
				alert.setContentText("This will remove the whole mod, this may cause issues if you do not know what you are doing!");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					String item = cell.getItem();
					File mod = new File(modsDir, item);
					mod.delete();
				}
			});
			contextMenu.getItems().addAll(disableMod, deleteMod);

			cell.textProperty().bind(cell.itemProperty());

			cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					cell.setContextMenu(null);
				} else {
					cell.setContextMenu(contextMenu);
				}
			});
			return cell;
		});
	}

	@Override
	protected void onStart() {

	}

	@Override
	public void refresh() {

	}
}
