package com.hearthproject.oneclient.fx.contentpane.base;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.nodes.ContentPaneButton;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

public abstract class ContentPane {
	public ContentPaneButton button;
	private Node node;
	private String fxmlFile;
	private FXMLLoader loader;
	private String name;

	public ContentPane(String fxmlFile, String name, String imageName, ButtonDisplay buttonDisplay) {
		button = new ContentPaneButton(imageName, buttonDisplay);
		button.setText(name.toUpperCase());
		button.prefWidthProperty().bind(Main.mainController.sideBox.widthProperty());
		button.setOnAction((actionHandler) -> {
			Main.mainController.contentPanes.stream().filter(s -> s != this).forEach(ContentPane::close);
			Main.mainController.setContent(this);
		});
		this.fxmlFile = fxmlFile;
		this.name = name;
		Main.mainController.contentPanes.add(this);
	}

	public Node getNode() {
		return node;
	}

	public void setNode() {
		try {
			node = getFXMLLoader().load(getFXMLUrl().openStream());
		} catch (IOException e) {
			OneClientLogging.error(e);
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

	public ContentPaneButton getButton() {
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

	public void close() {

	}
}
