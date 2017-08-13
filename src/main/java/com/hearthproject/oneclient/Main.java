package com.hearthproject.oneclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
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
        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.show();
        Controller controller = fxmlLoader.getController();
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> controller.onSceneResize(scene));
        scene.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> controller.onSceneResize(scene));
        controller.onStart(stage);
    }
}
