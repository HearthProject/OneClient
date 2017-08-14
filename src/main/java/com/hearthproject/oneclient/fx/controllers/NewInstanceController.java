package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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

    public static void start() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL fxmlUrl = classLoader.getResource("gui/newInstance.fxml");
            if (fxmlUrl == null) {
                System.out.println("An error has occurred loading newInstance.fxml!");
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
            e.printStackTrace();
        }
    }

    public void onStart(Stage stage) {
        reloadMCVerList();
        instanceNameField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            //Todo: Check if newValue contains any characters other than A-Z, 0-9, Space, Underscore, Parenthesis, and Hyphen
        });
    }

    public void onCreateButtonPress() {
    	Instance instance = new Instance(instanceNameField.getText());
    	instance.minecraftVersion = mcVersionComboBox.getSelectionModel().getSelectedItem().toString();
    	InstanceManager.addInstance(instance);
    	stage.close();
    	Main.mainController.refreshInstances();
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

    }

    public void reloadMCVerList() {
        try {
	        mcVersionComboBox.getItems().clear();
            GameVersion gameVersion = MinecraftUtil.loadGameVersion();
	        gameVersion.versions.stream().filter(version -> version.type.equals("release") || showSnapshotCheckBox.isSelected()).forEach(version -> mcVersionComboBox.getItems().add(version.id));
            mcVersionComboBox.getSelectionModel().selectFirst();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onModLoaderComboBoxChange() {

    }
}
