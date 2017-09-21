package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.curse.data.CurseProject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PackUpdateDialog extends TableDialog<CurseProject.CurseFile> {

	@SuppressWarnings("unchecked")
	public PackUpdateDialog(List<CurseProject.CurseFile> files) {
		super(files);
		TableColumn<CurseProject.CurseFile, String> columnName = new TableColumn<>("Files");
		columnName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFileName()));

		TableColumn<CurseProject.CurseFile, String> columnVersion = new TableColumn<>("Version");
		columnVersion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGameVersion().stream().collect(Collectors.joining())));
		TableColumn<CurseProject.CurseFile, Date> columnDate = new TableColumn<>("Release Date");
		columnDate.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDate()));

		table.getColumns().addAll(columnName, columnVersion, columnDate);

		setTitle("Pack Update Dialog");
		dialogPane.setHeaderText("Please Choose a Pack Version");
		dialogPane.getStyleClass().add("pack-update-dialog");
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
	}
}
