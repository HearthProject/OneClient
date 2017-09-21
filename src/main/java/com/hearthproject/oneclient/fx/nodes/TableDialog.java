package com.hearthproject.oneclient.fx.nodes;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;

public class TableDialog<T> extends Dialog<T> {

	protected final GridPane grid;
	protected final Label label;
	protected final TableView<T> table;

	protected final DialogPane dialogPane;

	@SuppressWarnings("unchecked")
	public TableDialog(List<T> files) {
		dialogPane = getDialogPane();
		setResizable(true);
		this.table = new TableView<>(FXCollections.observableArrayList(files));
		this.table.setMaxWidth(Double.MAX_VALUE);

		GridPane.setHgrow(table, Priority.ALWAYS);
		GridPane.setFillWidth(table, true);

		label = createContentLabel(dialogPane.getContentText());
		label.setPrefWidth(Region.USE_COMPUTED_SIZE);
		label.textProperty().bind(dialogPane.contentTextProperty());

		this.grid = new GridPane();
		this.grid.setHgap(10);
		this.grid.setMaxWidth(Double.MAX_VALUE);
		this.grid.setAlignment(Pos.CENTER_LEFT);

		dialogPane.contentTextProperty().addListener(o -> updateGrid());
		updateGrid();

		setResultConverter((dialogButton) -> {
			ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonBar.ButtonData.OK_DONE ? table.getSelectionModel().getSelectedItem() : null;
		});
	}

	public Label getLabel() {
		return label;
	}

	private void updateGrid() {
		grid.getChildren().clear();

		grid.add(label, 0, 0);
		grid.add(table, 1, 0);
		getDialogPane().setContent(grid);

		Platform.runLater(table::requestFocus);
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

	public TableView<T> getTable() {
		return table;
	}
}
