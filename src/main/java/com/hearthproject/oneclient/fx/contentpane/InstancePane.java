package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.Mod;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.NewInstanceController;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class InstancePane extends ContentPane {

	public TableView<Mod> tableMods;

	public Label textPackName;
	public Label textMinecraftVersion;
	public ImageView packIcon;
	public Button buttonPlay;
	public Button buttonOpenFolder;
	public Button buttonUpdate;
	public Button buttonEditVersion;
	public Button buttonGetCurseMods;
	public Button buttonBack;
	public Button buttonDelete;

	public Instance instance;

	public InstancePane() {
		super("gui/contentpanes/instance_view.fxml", "InstancePane", "", ButtonDisplay.NONE);
	}

	public static void show(Instance instance) {
		InstancePane pane = ContentPanes.getPane(InstancePane.class);
		pane.setupPane(instance);
		AnchorPane node = (AnchorPane) pane.getNode();
		node.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		node.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		VBox.setVgrow(node, Priority.ALWAYS);
		HBox.setHgrow(node, Priority.ALWAYS);
		Main.mainController.currentContent.button.setSelected(false);
		Main.mainController.setContent(pane);
	}

	@SuppressWarnings({ "unchecked", "ResultOfMethodCallIgnored" })
	public void setupPane(Instance instance) {
		this.instance = instance;
		textPackName.setText(instance.getName());
		textMinecraftVersion.setText("Minecraft " + instance.getGameVersion());

		packIcon.setImage(ImageUtil.openImage(instance.getIcon()));
		packIcon.setFitWidth(150);
		packIcon.setFitHeight(150);

		buttonOpenFolder.setOnAction(event -> OperatingSystem.openWithSystem(instance.getDirectory()));

		buttonPlay.setOnAction(event -> MinecraftUtil.startMinecraft(instance));

		buttonUpdate.setOnAction(event -> instance.update());
		buttonEditVersion.setOnAction(event -> NewInstanceController.start(instance));

		buttonBack.setOnAction(event -> ContentPanes.INSTANCES_PANE.button.fire());
		buttonGetCurseMods.setDisable(true);
		//		buttonGetCurseMods.setOnAction(event -> ModInstallingController.showInstaller(instance));
		buttonDelete.setOnAction(event -> {
			instance.delete();
			Main.mainController.setContent(ContentPanes.INSTANCES_PANE);
		});

		TableColumn<Mod, Boolean> columnEnabled = new TableColumn<>("Enabled");
		columnEnabled.setCellValueFactory(new PropertyValueFactory<>("enabled"));

		tableMods.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		tableMods.setPlaceholder(new Label("No Mods installed"));
		TableColumn<Mod, String> columnMods = new TableColumn<>("Mods");
		columnMods.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
		tableMods.setRowFactory(table -> {
			TableRow<Mod> row = new TableRow<>();
			final ContextMenu rowMenu = new ContextMenu();
			MenuItem open = new MenuItem("Open");

			open.setOnAction(event -> OperatingSystem.openWithSystem(row.getItem().getHash().getFile()));

			MenuItem disable = new MenuItem("Enable/Disable");
			disable.setOnAction(event -> row.getItem().getHash().getFile().renameTo(new File(row.getItem().getHash().getFile().toString() + ".disabled")));

			MenuItem delete = new MenuItem("Delete");
			delete.setOnAction(event -> {
				FileUtils.deleteQuietly(row.getItem().getHash().getFile());
				instance.mods.remove(row.getItem());
			});
			rowMenu.getItems().addAll(open, disable, delete);
			row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
			return row;
		});

		tableMods.setItems(instance.getMods());
		tableMods.sort();
		tableMods.getColumns().addAll(columnMods);
	}

	@Override
	protected void onStart() {

	}

	@Override
	public void refresh() {

	}

}
