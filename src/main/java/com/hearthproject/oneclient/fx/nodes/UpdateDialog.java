package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.modpack.curse.data.CurseFullProject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateDialog extends TableDialog<CurseFullProject.CurseFile> {

	@SuppressWarnings("unchecked")
	public UpdateDialog(List<CurseFullProject.CurseFile> files) {
		super(files);
		TableColumn<CurseFullProject.CurseFile, String> columnName = new TableColumn<>("Files");
		columnName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFileName()));

		TableColumn<CurseFullProject.CurseFile, String> columnVersion = new TableColumn<>("Version");
		columnVersion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGameVersion().stream().collect(Collectors.joining(", "))));
		TableColumn<CurseFullProject.CurseFile, Date> columnDate = new TableColumn<>("Release Date");
		columnDate.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDate()));

		table.getColumns().addAll(columnName, columnVersion, columnDate);

		setTitle("File Update Dialog");
		dialogPane.setHeaderText("Please Choose a File Version");
		dialogPane.getStyleClass().add("pack-update-dialog");
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
	}
}
