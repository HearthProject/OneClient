package com.hearthproject.oneclient;

import com.hearthproject.oneclient.util.launcher.InstanceUtil;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        loadData();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fxmlUrl = classLoader.getResource("gui/main.fxml");
        if (fxmlUrl == null) {
            System.out.println("An error has occurred!");
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxmlUrl);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load(fxmlUrl.openStream());
        stage.setTitle("One Client");
        stage.getIcons().add(new Image("icon.png"));
        Scene scene = new Scene(root, 1209, 800);
        stage.setScene(scene);
        stage.show();
        Controller controller = fxmlLoader.getController();
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> controller.onSceneResize(scene));
        scene.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> controller.onSceneResize(scene));
        controller.onStart(stage);
    }

    public void loadData() throws IOException {
        MinecraftUtil.loadGameVersion();
        InstanceUtil.getInstances();
    }
}
