package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.List;

public interface ProjectInfo {
    ComboBox<Database.ProjectFile> getCombo();

    Hyperlink getTitle();

    VBox getLeft();

    VBox getRight();

    ImageView getImageView();

    Button getInstallButton();

    default void setImage(String url, String name) {
        ImageUtil.ImageDownloadService service = new ImageUtil.ImageDownloadService(url, name);
        service.setOnSucceeded(event -> MiscUtil.runLaterIfNeeded(() -> getImageView().setImage(service.getValue())));
        service.start();
    }


    default void addLeftInfo(String format, Object... params) {
        if (getLeft() != null)
            getLeft().getChildren().add(info(String.format(format, params)));
    }

    default void addRightInfo(String format, Object... params) {
        if (getRight() != null)
            getRight().getChildren().add(info(String.format(format, params)));
    }


    default void setTitle(String name, String url) {
        MiscUtil.setupLink(getTitle(), name, url);
    }

    default void setFiles(List<Database.ProjectFile> files) {
        if (getCombo() != null) {
            getCombo().setVisible(true);
            getCombo().setItems(FXCollections.observableArrayList(files));
            getCombo().getSelectionModel().selectFirst();
        }
    }

    default Label info(String value) {
        Label l = new Label(value);
        l.setTextFill(Color.web("#FFFFFF"));
        l.setId("oc-info-label");
        l.setWrapText(true);
        l.setTextAlignment(TextAlignment.JUSTIFY);
        return l;
    }
}
