package com.hearthproject.oneclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Parent root = loader.load();
        stage.setTitle("One Client");
        Scene scene = new Scene(root, 1000, 800);
        Controller controller = loader.getController();
        controller.onStart(stage);
        stage.setScene(scene);
        stage.show();
    }
}
