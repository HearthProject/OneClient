package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.curse.data.CurseProject;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PackUpdateDialog extends Dialog<CurseProject.CurseFile> {

	private final GridPane grid;
	private final Label label;
	private final TableView<CurseProject.CurseFile> fileTable;

	@SuppressWarnings("unchecked")
	public PackUpdateDialog(List<CurseProject.CurseFile> files) {
		final DialogPane dialogPane = getDialogPane();
		setResizable(true);
		this.fileTable = new TableView<>(FXCollections.observableArrayList(files));
		this.fileTable.setMaxWidth(Double.MAX_VALUE);

		TableColumn<CurseProject.CurseFile, String> columnName = new TableColumn<>("Files");
		columnName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFileName()));

		TableColumn<CurseProject.CurseFile, String> columnVersion = new TableColumn<>("Version");
		columnVersion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGameVersion().stream().collect(Collectors.joining())));
		TableColumn<CurseProject.CurseFile, Date> columnDate = new TableColumn<>("Release Date");
		columnDate.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDate()));

		this.fileTable.getColumns().addAll(columnName, columnVersion, columnDate);

		GridPane.setHgrow(fileTable, Priority.ALWAYS);
		GridPane.setFillWidth(fileTable, true);

		label = createContentLabel(dialogPane.getContentText());
		label.setPrefWidth(Region.USE_COMPUTED_SIZE);
		label.textProperty().bind(dialogPane.contentTextProperty());

		this.grid = new GridPane();
		this.grid.setHgap(10);
		this.grid.setMaxWidth(Double.MAX_VALUE);
		this.grid.setAlignment(Pos.CENTER_LEFT);

		dialogPane.contentTextProperty().addListener(o -> updateGrid());

		setTitle("Pack Update Dialog");
		dialogPane.setHeaderText("Please Choose a Pack Version");
		dialogPane.getStyleClass().add("pack-update-dialog");
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		updateGrid();

		setResultConverter((dialogButton) -> {
			ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonBar.ButtonData.OK_DONE ? fileTable.getSelectionModel().getSelectedItem() : null;
		});
	}

	public Label getLabel() {
		return label;
	}

	private void updateGrid() {
		grid.getChildren().clear();

		grid.add(label, 0, 0);
		grid.add(fileTable, 1, 0);
		getDialogPane().setContent(grid);

		Platform.runLater(fileTable::requestFocus);
	}

	private static Label createContentLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.getStyleClass().add("content");
		label.setWrapText(true);
		label.setPrefWidth(360);
		return label;
	}
}
