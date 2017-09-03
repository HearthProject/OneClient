package com.hearthproject.oneclient.fx.contentpane.instance;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.ModInstallingController;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftAuth;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class InstancePane extends ContentPane {
	public ObservableList<String> mods = FXCollections.observableArrayList();

	public Label textPackName;
	public Label textMinecraftVersion;
	public ImageView packIcon;
	public Button buttonPlay;
	public Button buttonOpenFolder;
	public Button buttonExportPack;
	public Button buttonEditVersion;
	public Button buttonGetCurseMods;
	public Button buttonBack;
	public Button buttonDelete;
	public ListView<String> modList;
	public Instance instance;

	public InstancePane() {
		super("gui/contentpanes/instance_view.fxml", "InstancePane", "");
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
		textPackName.setText(instance.getManifest().getName());
		textMinecraftVersion.setText("Minecraft " + instance.getManifest().getMinecraftVersion());

		packIcon.setImage(ImageUtil.openImage(instance.getManifest().getIcon()));
		packIcon.setFitWidth(150);
		packIcon.setFitHeight(150);

		updateList();

		buttonOpenFolder.setOnAction(event -> OperatingSystem.openWithSystem(instance.getDirectory()));

		buttonPlay.setOnAction(event -> MinecraftAuth.loginAndPlay(instance));

		buttonExportPack.setOnAction(event -> instance.export());
		buttonEditVersion.setOnAction(event -> NewInstanceController.start(instance));

		buttonBack.setOnAction(event -> ContentPanes.INSTANCES_PANE.button.fire());
		buttonGetCurseMods.setOnAction(event -> ModInstallingController.showInstaller(instance));
		buttonDelete.setOnAction(event -> instance.delete());
	}

	public void updateList() {

		File modsDir = new File(instance.getDirectory(), "mods");
		if (modsDir.exists()) {
			mods = Arrays.stream(modsDir.listFiles()).map(File::getName).collect(MiscUtil.toObservableList());
		}
		modList.setItems(mods);
		modList.setCellFactory(param -> {
			ModListCell cell = new ModListCell();
			ContextMenu contextMenu = new ContextMenu();
			MenuItem disableMod = new MenuItem("Disable/Enable mod");
			disableMod.setOnAction(event -> {
				try {
					String item = cell.getItem();
					File mod = new File(modsDir, item);
					if (mod.exists()) {
						if (item.endsWith(".disabled")) {
							FileUtils.moveFile(mod, new File(mod.getParent(), mod.getName().substring(0, mod.getName().length() - ".disabled".length())));
						} else {
							FileUtils.moveFile(mod, new File(mod.getParent(), mod.getName() + ".disabled"));
						}
						updateList();
					} else {
						disableMod.setDisable(true);
					}
				} catch (IOException e) {
					OneClientLogging.error(e);
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
					updateList();
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
		updateList();
	}

	public class Mod {
		public String name;

		public Mod(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
