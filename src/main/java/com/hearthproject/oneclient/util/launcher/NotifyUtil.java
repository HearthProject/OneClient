package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.controlsfx.control.StatusBar;

public class NotifyUtil {
	public static StatusBar getStatus() {
		return Main.mainController.statusBar;
	}

	public static void clear() {
		MiscUtil.runLaterIfNeeded(() -> {
			getStatus().setText(null);
			getStatus().setProgress(-1);
		});
	}

	public static void setText(String format, Object... params) {
		MiscUtil.runLaterIfNeeded(() -> {
			if (!format.isEmpty())
				OneClientLogging.info(String.format(format, params));
			getStatus().setText(String.format(format, params));
		});
	}

	public static void setText(Duration duration, String format, Object... params) {
		setText(format, params);
		PauseTransition delay = new PauseTransition(duration);
		delay.setOnFinished(e -> setText(""));
		delay.play();
	}

	public static void setProgress(double value) {
		MiscUtil.runLaterIfNeeded(() -> getStatus().progressProperty().setValue(value));
	}

	public static void setProgressDescend(int current, int max) {
		MiscUtil.runLaterIfNeeded(() -> OneClientLogging.info("{}/{}", current, max));
		setProgress(((max - current) / (double) max));
	}

	public static void setProgressAscend(int current, int max) {
		MiscUtil.runLaterIfNeeded(() -> OneClientLogging.info("{}/{}", current, max));
		setProgress((current / (double) max));
	}
}
