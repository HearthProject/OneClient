package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.cmdb.Database;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateDialog extends TableDialog<Database.ProjectFile> {

	@SuppressWarnings("unchecked")
    public UpdateDialog(List<Database.ProjectFile> files) {
        super(files);
        TableColumn<Database.ProjectFile, String> columnName = new TableColumn<>("Files");
        columnName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFilename()));

        TableColumn<Database.ProjectFile, String> columnVersion = new TableColumn<>("Version");
        columnVersion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVersions().stream().collect(Collectors.joining(", "))));
        TableColumn<Database.ProjectFile, Date> columnDate = new TableColumn<>("Release Date");
//TODO		columnDate.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDate()));

		table.getColumns().addAll(columnName, columnVersion, columnDate);

		setTitle("File Update Dialog");
		dialogPane.setHeaderText("Please Choose a File Version");
		dialogPane.getStyleClass().add("pack-update-dialog");
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
	}
}
