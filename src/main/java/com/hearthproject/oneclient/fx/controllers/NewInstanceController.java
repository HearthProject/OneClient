package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

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

	public Instance instance;

	public static void start(Instance instance) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/newInstance.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.logger.error("An error has occurred loading newInstance.fxml!");
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
			controller.instance = instance;
			controller.onStart(stage);
		} catch (Exception e) {
			OneClientLogging.logger.error(e);
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

		if (instance != null) {
			modLoaderComboBox.getSelectionModel().select(instance.modLoader);
			modLoaderVersionComboBox.getSelectionModel().select(instance.modLoaderVersion);
			mcVersionComboBox.getSelectionModel().select(instance.minecraftVersion);
			instanceNameField.setText(instance.name);
			instanceNameField.setEditable(false);
			if (instance.getIcon() != null && instance.getIcon().exists()) {
				try {
					InputStream targetStream = new FileInputStream(instance.getIcon());
					iconPreview.setImage(new Image(targetStream));
					targetStream.close();
				} catch (IOException e) {
					OneClientLogging.logger.error(e);
				}
			}
			createButton.setText("Update Instance");
		}
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
				OneClientLogging.logger.error(e);
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
		boolean newInstance = true;
		if (this.instance != null) {
			newInstance = false;
			instance = this.instance;
		}
		if (newInstance && !InstanceManager.isValid(instance)) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Error");
			alert.setHeaderText("That isnt a valid instance");
			alert.setContentText("Do you already have an instance with that name?");
			alert.showAndWait();
			return;
		}

		if (selectedImageFile != null) {
			instance.icon = selectedImageFile.getName();
			try {
				FileUtils.copyFile(selectedImageFile, instance.getIcon());
			} catch (IOException e) {
				OneClientLogging.logger.error(e);
			}
		}
		instance.minecraftVersion = mcVersionComboBox.getSelectionModel().getSelectedItem().toString();
		if (modLoaderVersionComboBox.getSelectionModel().getSelectedItem() != null && modLoaderComboBox.getSelectionModel().getSelectedItem() != null) {
			instance.modLoader = modLoaderComboBox.getSelectionModel().getSelectedItem().toString();
			instance.modLoaderVersion = modLoaderVersionComboBox.getSelectionModel().getSelectedItem().toString();
		}
		if (newInstance) {
			InstanceManager.addInstance(instance);
		} else {
			InstanceManager.save(instance);
		}

		stage.close();

		if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
			Main.mainController.currentContent.refresh();
		}

		Instance finalInstance = instance;
		new Thread(() -> {
			try {
				MinecraftUtil.installMinecraft(finalInstance);
				Platform.runLater(() -> ContentPanes.INSTANCES_PANE.button.fire());

			} catch (Throwable throwable) {
				OneClientLogging.logUserError(throwable, "An error occurred while installing minecraft");
			}
		}).start();

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
				targetStream.close();
			} catch (IOException e) {
				OneClientLogging.logger.error(e);
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
			OneClientLogging.logger.error(e);
		}
	}

	public void onModLoaderComboBoxChange() {

	}
}