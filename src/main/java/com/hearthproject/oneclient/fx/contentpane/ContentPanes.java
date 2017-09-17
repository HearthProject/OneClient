package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;

import java.net.URL;
import java.util.ArrayList;

public class ContentPanes {
	public static ArrayList<ContentPane> panesList = new ArrayList<>();
	public static final InstancesPane INSTANCES_PANE = getPane(InstancesPane.class);
	public static final DownloadsPane DOWNLOADS_PANE = getPane(DownloadsPane.class);
	public static final SettingsPane SETTINGS_PANE = getPane(SettingsPane.class);
	public static final AboutPane ABOUT_PANE = getPane(AboutPane.class);
	public static final PrivatePackPane PRIVATE_PACK_PANE = getPane(PrivatePackPane.class);
	public static final CurseMetaPane CURSE_META_PANE = getPane(CurseMetaPane.class);
	public static final PackPane PACK_PANE = getPane(PackPane.class);
	
	public static <T extends ContentPane> T getPane(Class<T> clazz) {
		try {
			ContentPane pane = clazz.newInstance();
			URL fxmlUrl = pane.getFXMLUrl();
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			pane.setFXMLLoader(fxmlLoader);
			fxmlLoader.setController(pane);
			pane.setNode();
			if (pane.showInSideBar()) {
				panesList.add(pane);
			}
			return fxmlLoader.getController();
		} catch (Exception e) {
			OneClientLogging.error(e);
		}
		return null;
	}
}