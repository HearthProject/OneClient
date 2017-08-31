package com.hearthproject.oneclient.util.launcher;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.Notifications;

import static com.hearthproject.oneclient.util.MiscUtil.runLaterIfNeeded;

public class NotifyUtil {

	public static void notifier(String pTitle, String pMessage) {
		runLaterIfNeeded(() -> {
				Stage owner = new Stage(StageStyle.TRANSPARENT);
				StackPane root = new StackPane();
				root.setStyle("-fx-background-color: TRANSPARENT");
				Scene scene = new Scene(root, 1, 1);
				scene.setFill(Color.TRANSPARENT);
				owner.setScene(scene);
				owner.setWidth(1);
				owner.setHeight(1);
				owner.toBack();
				owner.show();
				Notifications.create().title(pTitle).text(pMessage).show();
			}
		);
	}

}
