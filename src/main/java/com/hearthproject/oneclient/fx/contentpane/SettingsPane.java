package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.nodes.JavaDialog;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.JavaUtil;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;

public class SettingsPane extends ContentPane {

	public CheckBox checkLog;
	public CheckBox checkTracking;
	public CheckBox checkCloseLauncher;
	public Button buttonSave;
	public TextField argumentBox;
	public TextField wrapperBox;
	public Spinner<Integer> spinnerMinRAM, spinnerMaxRAM;

	public SpinnerValueFactory.IntegerSpinnerValueFactory minMemory, maxMemory;

	public TextField fieldJavaPath;
	public Button buttonJavaPath, buttonFindJava;

	public SettingsPane() {
		super("gui/contentpanes/settings.fxml", "Settings", "settings.png", ButtonDisplay.BELOW_DIVIDER);
	}

	@Override
	protected void onStart() {

		checkLog.setSelected(SettingsUtil.settings.show_log_window);
		checkTracking.setSelected(SettingsUtil.settings.tracking);
		checkCloseLauncher.setSelected(SettingsUtil.settings.close_launcher_with_minecraft);
		buttonSave.setOnAction(event -> {
			SettingsUtil.settings.show_log_window = checkLog.isSelected();
			SettingsUtil.settings.tracking = checkTracking.isSelected();
			SettingsUtil.settings.close_launcher_with_minecraft = checkCloseLauncher.isSelected();
			SettingsUtil.settings.minecraftMinMemory = minMemory.getValue();
			SettingsUtil.settings.minecraftMaxMemory = maxMemory.getValue();
			SettingsUtil.settings.arguments = argumentBox.getText();
			SettingsUtil.settings.wrapperCommand = wrapperBox.getText();
			try {
				SettingsUtil.saveSetting();
			} catch (IOException e) {
				OneClientLogging.error(e);
			}
			if (SettingsUtil.settings.show_log_window) {
				OneClientLogging.showLogWindow();
			} else {
				OneClientLogging.hideLogWindow();
			}
		});

		maxMemory = new SpinnerValueFactory.IntegerSpinnerValueFactory(128, (int) OperatingSystem.getOSTotalMemory(), SettingsUtil.settings.minecraftMaxMemory, 128);
		minMemory = new SpinnerValueFactory.IntegerSpinnerValueFactory(128, (int) OperatingSystem.getOSTotalMemory(), SettingsUtil.settings.minecraftMinMemory, 128);

		maxMemory.valueProperty().addListener((observableValue, a, b) -> {
			if (minMemory.getValue() > maxMemory.getValue())
				minMemory.setValue(maxMemory.getValue());
		});
		minMemory.valueProperty().addListener((observableValue, a, b) -> {
			if (minMemory.getValue() > maxMemory.getValue())
				maxMemory.setValue(minMemory.getValue());
		});

		spinnerMinRAM.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				spinnerMinRAM.increment(0); // won't change value, but will commit editor
			}
		});

		spinnerMaxRAM.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				spinnerMaxRAM.increment(0); // won't change value, but will commit editor
			}
		});

		spinnerMinRAM.setValueFactory(minMemory);
		spinnerMaxRAM.setValueFactory(maxMemory);

		spinnerMinRAM.setEditable(true);
		spinnerMaxRAM.setEditable(true);

		argumentBox.setText(SettingsUtil.settings.arguments);
		buttonFindJava.setOnAction(event -> {
			JavaUtil.JavaInstall java = new JavaDialog().showAndWait().orElse(null);
			if (java != null)
				SettingsUtil.settings.setJavaPath(java.path);
		});
		Bindings.bindBidirectional(fieldJavaPath.textProperty(), SettingsUtil.settings.javaPath);

		buttonJavaPath.setOnAction(this::openChooser);

	}

	@Override
	public void refresh() {

	}

	public void openChooser(Event event) {
		selectJava(OperatingSystem.getPrograms());
	}

	public void selectJava(File start) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(start);
		chooser.setTitle("Choose Java Path");
		File file = chooser.showDialog(null);
		if (file != null) {
			if (!new File(file, "bin/java").exists()) {
				new Alert(Alert.AlertType.ERROR, "Invalid Java Path", ButtonType.OK).showAndWait();
				selectJava(file);
				return;
			}
			SettingsUtil.settings.setJavaPath(file.toString());
		}
	}
}
