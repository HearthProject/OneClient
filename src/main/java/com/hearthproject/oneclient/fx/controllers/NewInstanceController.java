package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;

public class NewInstanceController {
	public static Stage stage;
	@FXML
	public ImageView iconPreview;
	@FXML
	public TextField instanceNameField;
	@FXML
	public Button chooseIconButton;
	@FXML
	public ComboBox mcVersionComboBox;
	@FXML
	public CheckBox showSnapshotCheckBox;
	@FXML
	public ComboBox modLoaderComboBox;
	@FXML
	public ComboBox modLoaderVersionComboBox;
	@FXML
	public Button createButton;
	public File selectedImageFile;

	public static void start() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/newInstance.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.log("An error has occurred loading newInstance.fxml!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent root = fxmlLoader.load(fxmlUrl.openStream());
			stage = new Stage();
			stage.setTitle("One Client - Create New Instance");
			stage.getIcons().add(new Image("icon.png"));
			stage.setResizable(false);
			stage.initOwner(Main.stage);
			stage.initModality(Modality.WINDOW_MODAL);
			Scene scene = new Scene(root, 600, 300);
			scene.getStylesheets().add("gui/css/theme.css");
			stage.setScene(scene);
			stage.show();
			NewInstanceController controller = fxmlLoader.getController();
			controller.onStart(stage);
		} catch (Exception e) {
			OneClientLogging.log(e);
		}
	}

	public void onStart(Stage stage) {
		reloadMCVerList();
		instanceNameField.textProperty().addListener((observableValue, oldValue, newValue) -> {
			//Todo: Check if newValue contains any characters other than A-Z, 0-9, Space, Underscore, Parenthesis, and Hyphen
		});
		modLoaderComboBox.getItems().clear();
		modLoaderComboBox.getItems().add("Forge");
		modLoaderComboBox.getItems().add("None");
		modLoaderComboBox.getSelectionModel().selectFirst();

		modLoaderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshModLoader());

		mcVersionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshModLoader());

		refreshModLoader();
	}

	public void refreshModLoader() {
		if (modLoaderComboBox.getValue().toString().equalsIgnoreCase("Forge")) {
			modLoaderVersionComboBox.setDisable(false);
			modLoaderVersionComboBox.getItems().clear();
			try {
				if (mcVersionComboBox.getSelectionModel().getSelectedItem() != null) {
					ForgeUtils.loadForgeVersions().number.entrySet().stream()
						.filter(entry -> entry.getValue().mcversion.equalsIgnoreCase(mcVersionComboBox.getSelectionModel().getSelectedItem().toString()))
						.sorted(Comparator.comparingInt(o -> -o.getValue().build))
						.forEach(stringForgeVersionEntry -> modLoaderVersionComboBox.getItems().add(stringForgeVersionEntry.getValue().version));
				}
			} catch (IOException e) {
				OneClientLogging.log(e);
			}
			if (modLoaderVersionComboBox.getItems().isEmpty()) {
				modLoaderVersionComboBox.setDisable(true);
			} else {
				modLoaderVersionComboBox.getSelectionModel().selectFirst();
			}

		} else {
			modLoaderVersionComboBox.setDisable(true);
		}
	}

	public void onCreateButtonPress() {
		Instance instance = new Instance(instanceNameField.getText());
		if (selectedImageFile != null) {
			instance.icon = selectedImageFile.getPath();
		}
		instance.minecraftVersion = mcVersionComboBox.getSelectionModel().getSelectedItem().toString();
		if (modLoaderVersionComboBox.getSelectionModel().getSelectedItem() != null && modLoaderComboBox.getSelectionModel().getSelectedItem() != null) {
			instance.modLoader = modLoaderComboBox.getSelectionModel().getSelectedItem().toString();
			instance.modLoaderVersion = modLoaderVersionComboBox.getSelectionModel().getSelectedItem().toString();
		}
		InstanceManager.addInstance(instance);
		stage.close();

		if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
			Main.mainController.currentContent.refresh();
		}

		new Thread(() -> {
			try {
				MinecraftUtil.installMinecraft(instance);
			} catch (Throwable throwable) {
				OneClientLogging.log(throwable);
			}
		}).start();
		//TODO check the instnace can be added and is unique
		//        if (!instanceNameField.getText().isEmpty()) {
		//            for (Instance instance : InstanceManager.getInstances()) {
		//                if (!instance.name.equals(instanceNameField.getText())) {
		//
		//                }
		//            }
		//        }
	}

	public void onChooseIconButtonPress() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("One Client - Choose Instance Icon");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png"));
		selectedImageFile = fileChooser.showOpenDialog(stage);
		if (selectedImageFile != null) {
			try {
				InputStream targetStream = new FileInputStream(selectedImageFile);
				iconPreview.setImage(new Image(targetStream));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void reloadMCVerList() {
		try {
			mcVersionComboBox.getItems().clear();
			GameVersion gameVersion = MinecraftUtil.loadGameVersions();
			gameVersion.versions.stream().filter(version -> version.type.equals("release") || showSnapshotCheckBox.isSelected()).forEach(version -> mcVersionComboBox.getItems().add(version.id));
			mcVersionComboBox.getSelectionModel().selectFirst();
		} catch (Exception e) {
			OneClientLogging.log(e);
		}
	}

	public void onModLoaderComboBoxChange() {

	}
}