package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;

import java.net.URL;
import java.util.ArrayList;

public class ContentPanes {
	public static ArrayList<ContentPane> panesList = new ArrayList<>();
	public static final InstancesPane INSTANCES_PANE = (InstancesPane) getPane(InstancesPane.class);
	//public static final GetContentPane GET_CONTENT_PANE = (GetContentPane) getPane(GetContentPane.class);
	public static final CursePacksPane CURSE_PACKS_PANE = (CursePacksPane) getPane(CursePacksPane.class);

	static ContentPane getPane(Class<? extends ContentPane> clazz) {
		try {
			ContentPane pane = clazz.newInstance();
			URL fxmlUrl = pane.getFXMLUrl();
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			pane.setFXMLLoader(fxmlLoader);
			fxmlLoader.setController(pane);
			pane.setNode();
			panesList.add(pane);
			return fxmlLoader.getController();
		} catch (Exception e) {
			OneClientLogging.log(e);
		}
		return null;
	}
}