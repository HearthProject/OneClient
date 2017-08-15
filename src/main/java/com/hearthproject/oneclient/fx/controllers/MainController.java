package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainController {
    @FXML
    public Rectangle tabBar;
    @FXML
    public VBox contentPane;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public HBox barBox;
    @FXML
    public StackPane barPane;
    public Content currentContent = null;
    public ContentPaneController contentPaneController = null;


    public void onStart(Stage stage) throws IOException {
        setContent(Content.INSTANCES);
    }

    public void onSceneResize(Scene scene) {
        tabBar.setWidth(scene.getWidth());
        scrollPane.setPrefHeight(scene.getHeight() - (6 * 3) - barPane.getHeight());
    }

    public void setContent(Content content) {
        if (content == null) {
            contentPane.getChildren().clear();
        } else {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL fxmlUrl = classLoader.getResource(content.fxmlFile);
                if (fxmlUrl == null) {
                    OneClientLogging.log("An error has occurred loading " + content.fxmlFile.substring(4, content.fxmlFile.length() - 5) + "!");
                    return;
                }
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(fxmlUrl);
                fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
                contentPane.getChildren().setAll(fxmlLoader.<Node>load(fxmlUrl.openStream()));
                currentContent = Content.INSTANCES;
                contentPaneController = fxmlLoader.getController();
                contentPaneController.controller = this;
                contentPaneController.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public enum Content {
        INSTANCES, SETTINGS;

        String fxmlFile;

        Content(String fxmlFile) {
            this.fxmlFile = fxmlFile;
        }

        Content() {
            this.fxmlFile = "gui/" + this.name().toLowerCase() + ".fxml";
        }
    }
}
