package com.hearthproject.oneclient.fx.nodes.dialog;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class InstallDialog extends Dialog<File> {
    protected final GridPane grid;
    protected final TextField text;
    protected final Button buttonSelect;
    protected final DialogPane dialogPane;
    private SimpleObjectProperty<File> file;

    public InstallDialog() {
        dialogPane = getDialogPane();
        setResizable(true);

        DirectoryChooser chooser = new DirectoryChooser();
        File home = new File(System.getProperty("user.home"));
        chooser.setInitialDirectory(home);
        chooser.setTitle("Select OneClient Directory");
        buttonSelect = new Button("...");
        buttonSelect.setOnAction(event -> file.set(chooser.showDialog(null)));

        file = new SimpleObjectProperty<>(new File(home, "OneClient"));

        text = new TextField();
        text.setMaxWidth(Double.MAX_VALUE);
        text.textProperty().bind(file.asString());
        text.setEditable(true);

        GridPane.setHgrow(text, Priority.ALWAYS);
        GridPane.setFillWidth(text, true);

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());
        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? new File(text.getText()) : null;
        });

        dialogPane.setHeaderText("Choose OneClient Directory");
        dialogPane.getStyleClass().add("oc-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.getStylesheets().add("gui/css/theme.css");
    }


    private void updateGrid() {
        grid.getChildren().clear();
        grid.add(buttonSelect, 0, 0);
        grid.add(text, 1, 0);
        getDialogPane().setContent(grid);
        Platform.runLater(text::requestFocus);
    }


}
