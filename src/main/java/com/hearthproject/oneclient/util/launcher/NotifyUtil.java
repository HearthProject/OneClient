package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class NotifyUtil {

	public static void clear() {
		MiscUtil.runLaterIfNeeded(() -> setProgressText(""));
	}

	public static void setText(String format, Object... params) {
		MiscUtil.runLaterIfNeeded(() -> {
			if (!format.isEmpty())
				OneClientLogging.info(String.format(format, params));
		});
	}

	public static void setText(Duration duration, String format, Object... params) {
		setText(format, params);
		PauseTransition delay = new PauseTransition(duration);
		delay.setOnFinished(e -> setText(""));
		delay.play();
	}

	public static void setProgress(double value) {
	}

	public static void setProgressDescend(int current, int max) {
		setProgressText(String.format("%s/%s", current, max));
		setProgress(((max - current) / (double) max));
	}

	public static void setProgressAscend(int current, int max) {
		setProgressText(String.format("%s/%s", current, max));
		setProgress((current / (double) max));
	}

	public static void setProgressText(String value) {

	}

}
