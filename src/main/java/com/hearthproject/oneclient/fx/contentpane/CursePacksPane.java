package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.InstallingController;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class CursePacksPane extends ContentPane {
    public WebView webView;
    public Button buttonBrowse;
    public Button buttonInstall;
    public Button buttonImport;

    public HBox hBox;

    public CursePacksPane() {
        super("gui/contentpanes/getCurseContent.fxml", "Curse Modpacks", "#2D4BAD");
    }

    @Override
    protected void onStart() {
        webView.getEngine().load("https://minecraft.curseforge.com/modpacks");

        webView.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        webView.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
        webView.maxWidthProperty().bind(Main.mainController.contentBox.widthProperty());
        webView.maxHeightProperty().bind(Main.mainController.contentBox.heightProperty());

        AnchorPane box = (AnchorPane) getNode();

        VBox.setVgrow(getNode(), Priority.ALWAYS);
        HBox.setHgrow(webView, Priority.ALWAYS);
        HBox.setHgrow(getNode(), Priority.ALWAYS);

        box.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());

        WebEngine webEngine = webView.getEngine();

        buttonBrowse.setOnAction(event -> webView.getEngine().load("https://minecraft.curseforge.com/modpacks"));

        buttonInstall.setOnAction(event -> {
            System.out.println("installing");
            String url = webEngine.getLocation();
            if (!url.startsWith("https://minecraft.curseforge.com/projects/")) {
                System.out.println("This is not a pack!");
                return;
            }
            install(instance -> new CursePackInstaller().downloadFromURL(url, "latest", instance));
        });

        buttonImport.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Import Modpack From Zip");
            File zipFile = chooser.showOpenDialog(InstallingController.stage);
            if(zipFile == null || !zipFile.exists())
                return;
            install(instance -> new CursePackInstaller().importFromZip(instance,zipFile));
        });
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
            try {
                downloadFunction.accept(instance);
                MinecraftUtil.installMinecraft(instance);
            } catch (Throwable throwable) {
                OneClientLogging.log(throwable);
            }

            Platform.runLater(() -> {
                instance.icon = "icon.png";
//					Causes core dump on ubuntu
//					try {
//						FileUtils.copyURLToFile(new URL(webView.getEngine().executeScript("document.getElementsByClassName(\"e-avatar64 lightbox\")[0].href").toString()), instance.getIcon());
//					} catch (IOException e) {
//						e.printStackTrace();
//					}

                InstanceManager.addInstance(instance);
                if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
                    Main.mainController.currentContent.refresh();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pack has been installed!");
                alert.setHeaderText(null);
                alert.setContentText(instance.name + " has been downloaded and installed! You can find it in the instance section.");
                alert.showAndWait();
            });

        }).start();
    }

    @Override
    public void refresh() {

    }
}
