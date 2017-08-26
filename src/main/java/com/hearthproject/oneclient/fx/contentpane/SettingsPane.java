package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.IOException;

public class SettingsPane extends ContentPane {

	public CheckBox checkLog;
	public CheckBox checkTracking;
	public Button buttonSave;
	public Text memoryText;
	public Slider memorySlider;
	public TextField argumentBox;

	public SettingsPane() {
		super("gui/contentpanes/settings.fxml", "Settings", "#9C27B0");
	}

	@Override
	protected void onStart() {
		checkLog.setSelected(SettingsUtil.settings.show_log_window);
		checkTracking.setSelected(SettingsUtil.settings.tracking);
		buttonSave.setOnAction(event -> {
			SettingsUtil.settings.show_log_window = checkLog.isSelected();
			SettingsUtil.settings.tracking = checkTracking.isSelected();
			SettingsUtil.settings.minecraftMemory = (int) memorySlider.getValue();
			SettingsUtil.settings.arguments = argumentBox.getText();
			try {
				SettingsUtil.saveSetting();
			} catch (IOException e) {
				OneClientLogging.log(e);
			}
			if (SettingsUtil.settings.show_log_window) {
				OneClientLogging.showLogWindow();
			} else {
				OneClientLogging.hideLogWindow();
			}
		});
		if (OperatingSystem.getOSTotalMemory() != 0) {
			memorySlider.setMax(OperatingSystem.getOSTotalMemory());
		}
		memorySlider.setValue(SettingsUtil.settings.minecraftMemory);
		memoryText.setText("Allocated Memory: " + MiscUtil.round((memorySlider.getValue() / 1024), 2) + "GB");
		memorySlider.valueProperty().addListener((observable, oldValue, newValue) -> memoryText.setText("Allocated Memory: " + MiscUtil.round((memorySlider.getValue() / 1024), 2) + "GB"));
		argumentBox.setText(SettingsUtil.settings.arguments);
	}

	@Override
	public void refresh() {

	}
}
