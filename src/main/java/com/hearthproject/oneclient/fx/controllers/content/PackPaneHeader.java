package com.hearthproject.oneclient.fx.controllers.content;

import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.json.models.launcher.ModPack;
import com.hearthproject.oneclient.util.launcher.PackUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PackPaneHeader extends ContentPaneController {
	public TextField searchBox;

	static Map<String, Image> imageMap = new HashMap<>();
	static boolean search;
	static String seachTerm;
	static boolean canceUpdate = false;

	static MainController staticController;
	public VBox packlistBox;

	@Override
	protected void onStart() {
		staticController = controller;
		updatePackList();
		searchBox.textProperty().addListener((observable, oldValue, newValue) -> updatePackList());
	}

	static Thread reloadThread = createUpdateThread();

	private static Thread createUpdateThread() {
		return new Thread(() -> {
			try {

				try {
					List<String> nameList = new ArrayList<>();
					if(search){
						Query q = new QueryParser("title", PackUtil.analyzer).parse(seachTerm);
						IndexReader reader = DirectoryReader.open(PackUtil.index);
						IndexSearcher searcher = new IndexSearcher(reader);
						TopDocs docs = searcher.search(q, 50);
						ScoreDoc[] hits = docs.scoreDocs;
						for (int i = 0; i < hits.length; i++) {
							Document d = searcher.doc(hits[i].doc);
							nameList.add(d.get("title"));
						}
					}
					for(ModPack modPack : PackUtil.loadModPacks().packs){
						if(canceUpdate){
							break;
						}
						if(search){
							if(!nameList.contains(modPack.name)){
								continue;
							}
						}
						if(canceUpdate){
							break;
						}
						Platform.runLater(() -> addPackCard(modPack));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				OneClientLogging.log(e);
			}
		});
	}


	public void updatePackList(){
		search = !searchBox.getText().isEmpty();
		seachTerm = searchBox.getText();

		Node sNode = controller.contentPane.getChildren().get(0);
		controller.contentPane.getChildren().removeIf(node -> node != sNode);
		canceUpdate = true;
		try {
			reloadThread.join();
		} catch (InterruptedException e) {
			OneClientLogging.log(e);
		}
		canceUpdate = false;
		reloadThread = createUpdateThread();
		reloadThread.start();
	}

	@Override
	public void refresh() {

	}

	public static void addPackCard(ModPack modPack){
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/modpacklist/packcard.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.log("An error has occurred loading the mod pack card!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			staticController.contentPane.getChildren().add(fxmlLoader.load(fxmlUrl.openStream()));
			PackCardController packCardController = fxmlLoader.getController();
			packCardController.modpackName.setText(modPack.name);
			packCardController.modpackDetails.setText(modPack.authors);
			packCardController.modpackDescription.setText(modPack.description);
			Image image = null;
			if(imageMap.containsKey(modPack.name)){
				image = imageMap.get(modPack.name);
			} else {
				image = new Image(new URL(modPack.iconUrl).openStream());
				imageMap.put(modPack.name, image);
			}
			if(image != null){
				packCardController.modpackImage.setImage(image);
			}

		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}
}
