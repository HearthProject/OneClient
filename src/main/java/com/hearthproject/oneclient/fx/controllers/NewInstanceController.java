package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class NewInstanceController {
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

    public void onStart(Stage stage) {
        instanceNameField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            //Todo: Check if newValue contains any characters other than A-Z, 0-9, Space, Underscore, Parenthesis, and Hyphen
        });
    }

    public void onCreateButtonPress() {
        if (!instanceNameField.getText().isEmpty()) {
            for (Instance instance : InstanceManager.getInstances()) {
                if (!instance.name.equals(instanceNameField.getText())) {

                }
            }
        }
    }

    public void onChooseIconButtonPress() {

    }

    public void onShowSnapshotsChange() {

    }

    public void onModLoaderComboBoxChange() {

    }
}
