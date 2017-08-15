package com.hearthproject.oneclient;

import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.launcher.PackUtil;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    public static Stage stage;
    public static MainController mainController;

    public static void main(String... args) {
        SettingsUtil.init();
        System.out.println(SettingsUtil.settings.left_align_window_buttons.name + "=" + SettingsUtil.settings.left_align_window_buttons.setting.toString());
        SettingsUtil.updateSetting("left_align_window_buttons", false);
        System.out.println(SettingsUtil.settings.left_align_window_buttons.name + "=" + SettingsUtil.settings.left_align_window_buttons.setting.toString());
        Platform.runLater(() -> {
            OneClientLogging.setupLogController();
            OneClientLogging.showLogWindow();//TODO config
            try {
                SplashScreen.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        launch(args);
    }

    @Override
    public void start(Stage s) {
        stage = s;
        new Thread(() -> {
            try {
                loadData();
                SplashScreen.hide();
                Platform.runLater(() -> {
                    try {
                        startLauncher();
                    } catch (Exception e) {
                        OneClientLogging.log(e);
                    }
                });
            } catch (Exception e) {
                OneClientLogging.log(e);
            }
        }).start();

    }

    public void startLauncher() throws Exception {
        OneClientLogging.log("Starting One Client");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fxmlUrl = classLoader.getResource("gui/main.fxml");
        if (fxmlUrl == null) {
            OneClientLogging.log("An error has occurred loading main.fxml!");
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxmlUrl);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load(fxmlUrl.openStream());
        stage.setTitle("One Client");
        stage.getIcons().add(new Image("icon.png"));
        Scene scene = new Scene(root, 1221, 800);
        scene.getStylesheets().add("gui/css/theme.css");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((windowEvent) -> OneClientLogging.stage.close());
        mainController = fxmlLoader.getController();
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
        scene.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
        mainController.onStart(stage);

    }

    public void loadData() throws Exception {
        OneClientLogging.log("Loading instances");
        InstanceManager.load();
        OneClientLogging.log("Loading minecraft versions");
        MinecraftUtil.loadGameVersions();
        OneClientLogging.log("Loading forge versions");
        ForgeUtils.loadForgeVerions();
        OneClientLogging.log("Loading mod packs");
        PackUtil.loadModPacks();
        OneClientLogging.log("Done!");
        SplashScreen.updateProgess("Done!", 100);

    }
}
