package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.curse.CursePackImporter;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.*;

import java.io.IOException;

public class SettingsPane extends ContentPane {

	public CheckBox checkLog;
	public CheckBox checkTracking;
	public Button buttonSave;
	public TextField argumentBox;
	public Button buttonImport;
	public Spinner<Integer> spinnerMinRAM, spinnerMaxRAM;

	public SpinnerValueFactory.IntegerSpinnerValueFactory minMemory, maxMemory;

	public SettingsPane() {
		super("gui/contentpanes/settings.fxml", "Settings", "settings.png");
	}

	@Override
	protected void onStart() {
		checkLog.setSelected(SettingsUtil.settings.show_log_window);
		checkTracking.setSelected(SettingsUtil.settings.tracking);
		buttonSave.setOnAction(event -> {
			SettingsUtil.settings.show_log_window = checkLog.isSelected();
			SettingsUtil.settings.tracking = checkTracking.isSelected();
			SettingsUtil.settings.minecraftMinMemory = minMemory.getValue();
			SettingsUtil.settings.minecraftMaxMemory = maxMemory.getValue();
			SettingsUtil.settings.arguments = argumentBox.getText();
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

		maxMemory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, (int) OperatingSystem.getOSTotalMemory(), SettingsUtil.settings.minecraftMaxMemory, 128);
		minMemory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, (int) OperatingSystem.getOSTotalMemory(), SettingsUtil.settings.minecraftMinMemory, 128);

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
		buttonImport.setOnAction(event -> CursePackImporter.importPacks());
	}

	@Override
	public void refresh() {

	}

}
