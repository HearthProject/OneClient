package com.hearthproject.oneclient.fx.nodes.dialog;

import com.hearthproject.oneclient.util.files.JavaUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;

public class JavaDialog extends TableDialog<JavaUtil.JavaInstall> {

	public JavaDialog() {
		super(JavaUtil.getAvailableInstalls());

		TableColumn<JavaUtil.JavaInstall, String> paths = new TableColumn<>("Paths");
		paths.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().path));
		table.getColumns().addAll(paths);
		setTitle("Java Detection");
		dialogPane.setHeaderText("Please Choose a Java Version");
		dialogPane.getStyleClass().add("java-dialog");
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
	}

}
