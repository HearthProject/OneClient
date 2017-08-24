package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import java.io.IOException;

public class SettingsPane extends ContentPane {

	public CheckBox checkLog;
	public CheckBox checkTracking;
	public Button buttonSave;

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
	}

	@Override
	public void refresh() {

	}
}
