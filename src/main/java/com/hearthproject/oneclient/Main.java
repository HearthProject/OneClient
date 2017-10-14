package com.hearthproject.oneclient;

import com.hearthproject.oneclient.api.base.InstanceManager;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.fx.nodes.dialog.InstallDialog;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.launcher.Updater;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import com.hearthproject.oneclient.util.tracking.OneClientTracking;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main extends Application {
    public static Stage stage;
    public static MainController mainController;
    public static Scene scene;
    public static List<String> args;
    public static boolean nativeLaunch = false;

    public static void main(String... argArray) throws IOException {
        Main.args = Arrays.asList(argArray);
        launch(argArray);
    }

    private File getFile() {
        InstallDialog dialog = new InstallDialog();
        File file = dialog.showAndWait().orElse(null);
        if (file == null)
            System.exit(0);
        return file;
    }

    @Override
    public void start(Stage s) throws IOException {
        stage = s;
        if (Constants.PORTABLE) {
            Constants.RUN_DIR = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();
            Constants.setupDirs();
            startOneClient();
        }
        Constants.StaticSettings staticSettings = Constants.getSettings();
        if (staticSettings == null || !staticSettings.getInstallLocation().exists()) {
            Constants.RUN_DIR = getFile();
            if (Constants.RUN_DIR != null)
                Constants.saveSettings(new Constants.StaticSettings(Constants.RUN_DIR.toString()));
        } else {
            Constants.RUN_DIR = staticSettings.getInstallLocation();
        }
        Constants.setupDirs();
        startOneClient();
    }

    public void startOneClient() {
        SettingsUtil.init();
        OneClientLogging.logger.info("Starting OneClient version: " + Constants.getVersion());
        OneClientTracking.sendRequest("launch/" + Constants.getVersion());
        OneClientLogging.setupLogController();
        if (SettingsUtil.settings.show_log_window)
            OneClientLogging.showLogWindow();
        try {
            SplashScreen.show();
        } catch (IOException e) {
            OneClientLogging.error(e);
        }
        for (String arg : args) {
            if (arg.equals("-updateSuccess")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Update complete!");
                alert.setHeaderText("The update was successful!");
                alert.setContentText("You are now running OneClient version " + Constants.getVersion() + "!");
                alert.showAndWait();
            } else if (arg.equals("-native_launch")) {
                nativeLaunch = true;
            }
        }
        new Thread(() -> {
            try {
                loadData();
                SplashScreen.hide();
                Platform.runLater(() -> {
                    try {
                        startLauncher();
                        Optional<String> latestVersion = Updater.checkForUpdate();
                        if (latestVersion.isPresent() && !nativeLaunch) {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Update?");
                            alert.setContentText("OneClient version " + latestVersion.get() + " is available, you are using " + Constants.getVersion() + ". Would you like to update?");
                            alert.setHeaderText("An update is available!");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.get() == ButtonType.OK) {
                                stage.hide();
                                SplashScreen.show();
                                SplashScreen.updateProgess("Downloading update...", 0);
                                new Thread(Updater::startUpdate).start();
                            }
                        }
                    } catch (Exception e) {
                        OneClientLogging.error(e);
                    }
                });
            } catch (Exception e) {
                OneClientLogging.error(e);
            }
        }).start();
    }

    public void startLauncher() throws Exception {
        OneClientLogging.logger.info("Starting One Client");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fxmlUrl = classLoader.getResource("gui/main.fxml");
        if (fxmlUrl == null) {
            OneClientLogging.logger.error("An error has occurred loading main.fxml!");
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxmlUrl);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load(fxmlUrl.openStream());
        if (Constants.getVersion() == null) {
            stage.setTitle("One Client");
        } else {
            stage.setTitle("One Client " + Constants.getVersion());
        }
        stage.getIcons().add(new Image("images/icon.png"));
        scene = new Scene(root, 1288, 800);
        scene.getStylesheets().add("gui/css/theme.css");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            OneClientLogging.stage.close();
            OneClientLogging.logger.info("Goodbye");
            System.exit(0);
        });
        mainController = fxmlLoader.getController();
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
        scene.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> mainController.onSceneResize(scene));
        mainController.onStart(stage);
        MinecraftAuthController.load();
    }

    public void loadData() throws Exception {
        Curse.init();
        InstanceManager.load();
        MinecraftUtil.load();
        ForgeUtils.loadForgeVersions();
        SplashScreen.updateProgess("Authenticating with mojang", 90);
        SplashScreen.updateProgess("Done!", 100);
    }

}
