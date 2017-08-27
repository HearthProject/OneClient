package com.hearthproject.oneclient.fx.contentpane.base;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.io.IOException;
import java.net.URL;

public abstract class ContentPane {
	public ContentPaneButton button;
	private Node node;
	private String fxmlFile;
	private FXMLLoader loader;
	private String name;
	private String color;

	public ContentPane(String fxmlFile, String name, String color) {
		this.color = color;
		button = new ContentPaneButton(color);
		button.setText(name);
		button.prefWidthProperty().bind(Main.mainController.sideBox.widthProperty());
		button.setOnAction((actionHandler) -> {
			Main.mainController.currentContent.button.setSelected(false);
			Main.mainController.setContent(this);
			button.setSelected(true);
		});
		this.fxmlFile = fxmlFile;
		this.name = name;
		Main.mainController.contentPanes.add(this);
	}

	public ContentPane(String fxmlFile, String name) {
		this(fxmlFile, name, "#A8A8A8");
	}

	public Node getNode() {
		return node;
	}

	public void setNode() {
		try {
			node = getFXMLLoader().load(getFXMLUrl().openStream());
		} catch (IOException e) {
			OneClientLogging.logger.error(e);
		}
	}

	public FXMLLoader getFXMLLoader() {
		return loader;
	}

	public void setFXMLLoader(FXMLLoader loader) {
		this.loader = loader;
	}

	public URL getFXMLUrl() {
		return Thread.currentThread().getContextClassLoader().getResource(fxmlFile);
	}

	public Button getButton() {
		return button;
	}

	public String getFxmlFile() {
		return fxmlFile;
	}

	public String getName() {
		return name;
	}

	protected abstract void onStart();

	public abstract void refresh();

	public final void start() {
		button.setSelected(true);
		onStart();
		refresh();
	}

	public boolean showInSideBar() {
		return true;
	}
}
