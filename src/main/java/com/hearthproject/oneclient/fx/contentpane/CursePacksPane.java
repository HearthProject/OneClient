package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.fx.nodes.CurseTile;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.curse.CurseUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CursePacksPane extends ContentPane {
    public ScrollPane scroll;
    public VBox vBox;

    public CursePacksPane() {
        super("gui/contentpanes/getCurseContent.fxml", "Curse Modpacks", "#2D4BAD");
    }

    @Override
    protected void onStart() {
        AnchorPane box = (AnchorPane) getNode();
        VBox.setVgrow(getNode(), Priority.ALWAYS);
        HBox.setHgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(getNode(), Priority.ALWAYS);
        box.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        vBox.getChildren().clear();

        List<CurseTile> tiles = CurseUtils.getPacks().stream().map(p -> new CurseTile(this, p)).collect(Collectors.toList());
        vBox.getChildren().addAll(tiles);
    }

    public void install(MiscUtil.ThrowingConsumer<Instance> downloadFunction) {
        try {
            InstallingController.showInstaller();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InstallingController.controller.setTitleText("Installing...");
        InstallingController.controller.setDetailText("Preparing to install");

        new Thread(() -> {
            Instance instance = new Instance("Unknown");
            instance.icon = "icon.png";
            try {
                downloadFunction.accept(instance);
                MinecraftUtil.installMinecraft(instance);
            } catch (Throwable throwable) {
                OneClientLogging.log(throwable);
            }

            Platform.runLater(() -> {
                InstanceManager.addInstance(instance);
                if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
                    Main.mainController.currentContent.refresh();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pack has been installed!");
                alert.setHeaderText(null);
                alert.setContentText(instance.name + " has been downloaded and installed! You can find it in the pack section.");
                alert.showAndWait();
            });

        }).start();
    }

    @Override
    public void refresh() {

    }
}
