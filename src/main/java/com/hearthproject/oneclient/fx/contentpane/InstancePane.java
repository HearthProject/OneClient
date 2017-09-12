package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.HearthInstance;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.InstanceConfigController;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftAuth;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class InstancePane extends ContentPane {

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
	public TableView tableMods;
	public HearthInstance instance;

	public InstancePane() {
		super("gui/contentpanes/instance_view.fxml", "InstancePane", "", ButtonDisplay.NONE);
	}

	public static void show(HearthInstance instance) {
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

	@SuppressWarnings("unchecked")
	public void setupPane(HearthInstance instance) {
		this.instance = instance;
		textPackName.setText(instance.getName());
		textMinecraftVersion.setText("Minecraft " + instance.getGameVersion());

		packIcon.setImage(ImageUtil.openImage(instance.getIcon()));
		packIcon.setFitWidth(150);
		packIcon.setFitHeight(150);

		buttonOpenFolder.setOnAction(event -> OperatingSystem.openWithSystem(instance.getDirectory()));

		buttonPlay.setOnAction(event -> MinecraftAuth.loginAndPlay(instance));

		//		buttonExportPack.setOnAction(event -> instance.export());
		buttonEditVersion.setOnAction(event -> InstanceConfigController.start(instance));

		buttonBack.setOnAction(event -> ContentPanes.INSTANCES_PANE.button.fire());
		buttonDelete.setOnAction(event -> instance.delete());
		////		TableColumn<Mod, Boolean> columnEnabled = new TableColumn<>("Enabled");
		////		columnEnabled.setCellValueFactory(new PropertyValueFactory<>("enabled"));
		////
		////		tableMods.setPlaceholder(new Label("No Mods installed"));
		////		TableColumn<Mod, String> columnMods = new TableColumn<>("Mods");
		////		tableMods.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		////		columnMods.setCellValueFactory(new PropertyValueFactory<>("name"));
		////		tableMods.setRowFactory(table -> {
		////			TableRow<Mod> row = new TableRow<>();
		////			final ContextMenu rowMenu = new ContextMenu();
		////			MenuItem open = new MenuItem("Open");
		////			open.setOnAction(event -> OperatingSystem.openWithSystem(row.getItem().file));
		////
		////			MenuItem disable = new MenuItem("Enable/Disable");
		////			disable.setOnAction(event -> row.getItem().file.renameTo(new File(row.getItem().file.toString() + ".disabled")));
		////
		////			MenuItem delete = new MenuItem("Delete");
		////			delete.setOnAction(event -> {
		////				row.getItem().file.delete();
		////				mods.remove(row.getItem());
		////			});
		////
		////			rowMenu.getItems().addAll(open, delete);
		////			row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
		////
		////			return row;
		////		});
		////		mods = FXCollections.observableArrayList(instance.getMods());
		//		tableMods.setItems(mods);
		//
		//		tableMods.getColumns().addAll(columnMods, columnEnabled);
	}

	@Override
	protected void onStart() {

	}

	@Override
	public void refresh() {

	}

}
