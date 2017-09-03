package com.hearthproject.oneclient.fx.controllers;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.CheckTreeView;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class FileSelection extends Dialog<List<File>> {
	private final GridPane grid;
	private final CheckTreeView<File> directoryView;

	public FileSelection(File parent) {
		DialogPane dialogPane = this.getDialogPane();
		dialogPane.setMaxWidth(1.7976931348623157E308D);
		this.directoryView = new CheckTreeView<>();
		this.directoryView.setMaxWidth(1.7976931348623157E308D);
		CheckBoxTreeItem<File> root = createTree(new CheckBoxTreeItem<>(parent));
		root.setExpanded(true);
		directoryView.setRoot(root);
		GridPane.setHgrow(this.directoryView, Priority.ALWAYS);
		GridPane.setFillWidth(this.directoryView, true);
		this.grid = new GridPane();
		this.grid.setHgap(10.0D);
		this.grid.setMaxWidth(1.7976931348623157E308D);
		this.grid.setAlignment(Pos.CENTER_LEFT);
		dialogPane.contentTextProperty().addListener((o) -> this.updateGrid());
		this.setTitle(ControlResources.getString("Dialog.confirm.title"));
		dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
		dialogPane.getStyleClass().add("text-input-dialog");
		dialogPane.getButtonTypes().addAll(new ButtonType[] { ButtonType.APPLY, ButtonType.CANCEL });
		this.updateGrid();
		this.setResultConverter((dialogButton) -> {
			ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonData.APPLY ? this.getValues() : null;
		});
	}

	public List<File> getValues() {
		return this.directoryView.getCheckModel().getCheckedItems().stream().filter(t -> ((CheckBoxTreeItem) t).isSelected()).map(TreeItem::getValue).collect(Collectors.toList());

	}

	private void updateGrid() {
		this.grid.getChildren().clear();
		this.grid.add(this.directoryView, 0, 0);
		this.getDialogPane().setContent(this.grid);
	}

	private CheckBoxTreeItem<File> createTree(CheckBoxTreeItem<File> node) {
		File[] files = node.getValue().listFiles();
		if (files == null)
			return node;
		for (File file : files) {
			node.getChildren().add(createTree(new CheckBoxTreeItem<>(file)));
		}
		return node;
	}
}
