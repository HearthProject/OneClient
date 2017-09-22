package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.InstanceManager;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.modloader.IModloader;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.stream.Collectors;

public class NewInstanceController {

	public ObservableList<GameVersion.VersionData> minecraftVersions = FXCollections.observableArrayList();
	public ObservableList<IModloader> modloaderVersions = FXCollections.observableArrayList();

	public static Stage stage;
	@FXML
	public ImageView iconPreview;
	@FXML
	public TextField instanceNameField;
	@FXML
	public Button chooseIconButton;
	@FXML
	public ComboBox<GameVersion.VersionData> mcVersionComboBox;
	@FXML
	public ComboBox<IModloader> modloaderComboBox;
	@FXML
	public CheckBox showSnapshotCheckBox;
	@FXML
	public Button createButton;
	public File selectedImageFile;

	public Instance instance;

	public static void start(Instance instance) {
		//TODO DEDUPLICATE THIS
		if (!MinecraftAuthController.isUserValid()) {
			MinecraftAuthController.updateGui();
			//TODO replace with a login request
			OneClientLogging.alert("You must log into minecraft to play the game!", "You are not logged in!");
			return;
		}
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/instance_creation.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.logger.error("An error has occurred loading instance_creation.fxml!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent root = fxmlLoader.load(fxmlUrl.openStream());
			stage = new Stage();
			stage.setTitle("One Client - Create New Instance");
			stage.getIcons().add(new Image("images/icon.png"));
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
			OneClientLogging.error(e);
		}
	}

	public void onStart(Stage stage) {
		loadVersions();
		loadModloaderVersions();
		showSnapshotCheckBox.setOnAction(event -> loadVersions());
		mcVersionComboBox.valueProperty().addListener(((o, a, b) -> loadModloaderVersions()));
		if (instance != null) {
			mcVersionComboBox.getSelectionModel().select(MinecraftUtil.getVersionData(instance.getGameVersion()));
			modloaderComboBox.getSelectionModel().select(modloaderVersions.stream().filter(m -> m.getVersion().equalsIgnoreCase(instance.getForgeVersion())).findFirst().orElse(IModloader.NONE));
			instanceNameField.setText(instance.getName());
			iconPreview.setImage(ImageUtil.openCachedImage(instance.getIcon()));
			createButton.setText("Update Instance");
		} else {
			mcVersionComboBox.getSelectionModel().selectFirst();
			modloaderComboBox.getSelectionModel().selectFirst();
		}
	}

	public void onCreateButtonPress() {
		Instance instance = new Instance();
		instance.setName(instanceNameField.getText());
		boolean newInstance = true;
		if (this.instance != null) {
			newInstance = false;
			instance = this.instance;
		}
		if (newInstance && instance.getDirectory().exists()) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Error");
			alert.setHeaderText("That isn't a valid instance");
			alert.setContentText("Do you already have an instance with that name?");
			alert.showAndWait();
			return;
		}

		if (selectedImageFile != null) {
			instance.setIcon(selectedImageFile.getName());
			try {
				FileUtils.copyFile(selectedImageFile, instance.getIconFile());
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
		}

		instance.setGameVersion(mcVersionComboBox.getValue().id);
		instance.setForgeVersion(modloaderComboBox.getValue().getVersion());

		if (newInstance) {
			InstanceManager.addInstance(instance);
		} else {
			InstanceManager.save();
		}

		stage.close();

		if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
			Main.mainController.currentContent.refresh();
		}

		Instance finalInstance = instance;
		new Thread(() -> {
			try {
				MinecraftUtil.installMinecraft(finalInstance);
				NotifyUtil.setText(Duration.seconds(10), "%s has been downloaded and installed!", finalInstance.getName());
				finalInstance.setInstalling(false);
			} catch (Throwable throwable) {
				OneClientLogging.logUserError(throwable, "An error occurred while installing minecraft");
			}
		}).start();

	}

	public void onChooseIconButtonPress() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("One Client - Choose Instance Icon");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpeg", "*.gif"));
		selectedImageFile = fileChooser.showOpenDialog(stage);
		if (selectedImageFile != null) {
			try {
				InputStream targetStream = new FileInputStream(selectedImageFile);
				iconPreview.setImage(new Image(targetStream));
				targetStream.close();
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
		}
	}

	public void loadVersions() {
		try {
			GameVersion version = MinecraftUtil.getGameVersionData();
			if (version != null) {
				minecraftVersions = FXCollections.observableArrayList(version.get(v -> v.type.equals("release") || showSnapshotCheckBox.isSelected()).collect(Collectors.toList()));
				mcVersionComboBox.setItems(minecraftVersions);
				mcVersionComboBox.getSelectionModel().selectFirst();
			}
		} catch (Exception e) {
			OneClientLogging.error(e);
		}
	}

	public void loadModloaderVersions() {
		if (mcVersionComboBox.getValue() != null) {
			try {

				IModloader old = modloaderComboBox.getValue();
				modloaderVersions.clear();
				modloaderVersions.add(IModloader.NONE);
				modloaderVersions.addAll(ForgeUtils.loadForgeVersions().filterMCVersion(mcVersionComboBox.getValue()));
				modloaderComboBox.setItems(modloaderVersions);
				if (old != IModloader.NONE) {
					modloaderComboBox.getSelectionModel().select(2);
				} else {
					modloaderComboBox.getSelectionModel().selectFirst();
				}

			} catch (IOException e) {
				OneClientLogging.error(e);
			}
		}
	}

	public void onModLoaderComboBoxChange() {

	}
}