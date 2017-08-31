package com.hearthproject.oneclient.fx.nodes;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.StatusBar;

public class StatusBarSkin extends SkinBase<StatusBar> {

	private HBox leftBox;
	private HBox rightBox;
	private Label label;
	private ProgressBar progressBar;

	public StatusBarSkin(StatusBar statusBar) {
		super(statusBar);

		GridPane gridPane = new GridPane();

		leftBox = new HBox();
		leftBox.setAlignment(Pos.CENTER);
		leftBox.getStyleClass().add("left-items"); //$NON-NLS-1$

		rightBox = new HBox();
		rightBox.setAlignment(Pos.CENTER);
		rightBox.getStyleClass().add("right-items"); //$NON-NLS-1$

		progressBar = new ProgressBar();

		label = new Label();
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.textProperty().bind(statusBar.textProperty());
		label.graphicProperty().bind(statusBar.graphicProperty());
		label.getStyleClass().add("status-label"); //$NON-NLS-1$
		label.styleProperty().bind(getSkinnable().styleProperty());

		leftBox.getChildren().setAll(getSkinnable().getLeftItems());

		rightBox.getChildren().setAll(getSkinnable().getRightItems());

		statusBar.getLeftItems().addListener(
			(Observable evt) -> leftBox.getChildren().setAll(
				getSkinnable().getLeftItems()));

		statusBar.getRightItems().addListener(
			(Observable evt) -> rightBox.getChildren().setAll(
				getSkinnable().getRightItems()));

		GridPane.setFillHeight(leftBox, true);
		GridPane.setFillHeight(rightBox, true);
		GridPane.setFillHeight(label, true);
		GridPane.setFillHeight(progressBar, true);

		GridPane.setVgrow(leftBox, Priority.ALWAYS);
		GridPane.setVgrow(rightBox, Priority.ALWAYS);
		GridPane.setVgrow(label, Priority.ALWAYS);
		GridPane.setVgrow(progressBar, Priority.ALWAYS);

		GridPane.setHgrow(label, Priority.ALWAYS);

		gridPane.add(leftBox, 0, 0);
		gridPane.add(label, 1, 0);
		gridPane.add(progressBar, 2, 0);
		gridPane.add(rightBox, 4, 0);
		getChildren().add(gridPane);

		/**
		 * We want to remove the progressBar from the GridPane when no Task is
		 * being executed. We used to toggle its visibility but the progressBar
		 * was still there and messing with the alignment.
		 */
		progressBar.progressProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			if (newValue.doubleValue() > 0.0) {
				if (!gridPane.getChildren().contains(progressBar)) {
					gridPane.add(progressBar, 2, 0);

				}
			} else {
				gridPane.getChildren().remove(progressBar);
			}
		});
		progressBar.progressProperty().bind(statusBar.progressProperty());
	}

}
