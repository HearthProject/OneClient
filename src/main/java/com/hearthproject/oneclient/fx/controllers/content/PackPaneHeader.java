package com.hearthproject.oneclient.fx.controllers.content;

import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.json.models.curse.ModPacks;
import com.hearthproject.oneclient.json.models.launcher.ModPack;
import com.hearthproject.oneclient.util.curse.CurseUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputMethodEvent;
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
import java.util.function.Predicate;

public class PackPaneHeader extends ContentPaneController {
	public TextField searchBox;

	static Map<Integer, Image> imageMap = new HashMap<>();
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
					List<Integer> idList = new ArrayList<>();
					if(search){
						Query q = new QueryParser("title", CurseUtil.analyzer).parse(seachTerm);
						IndexReader reader = DirectoryReader.open(CurseUtil.index);
						IndexSearcher searcher = new IndexSearcher(reader);
						TopDocs docs = searcher.search(q, 50);
						ScoreDoc[] hits = docs.scoreDocs;
						for (int i = 0; i < hits.length; i++) {
							Document d = searcher.doc(hits[i].doc);
							idList.add(Integer.parseInt(d.get("id")));
						}
					}
					int i = 0;
					for(ModPacks.CursePack cursePack : CurseUtil.loadModPacks().Data){
						if(canceUpdate){
							break;
						}
						if(search){
							if(!idList.contains(cursePack.Id)){
								continue;
							}
						}
						if(i > 50){
							break;
						}
						i++;
						ModPack pack = new ModPack(cursePack);
						if(canceUpdate){
							break;
						}
						Platform.runLater(() -> addPackCard(pack, cursePack.Id));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				OneClientLogging.log(e);
			}
		});
	}


	static class ImageDownloadItem{
		public PackCardController controller;
		public String url;

		public ImageDownloadItem(PackCardController controller, String url) {
			this.controller = controller;
			this.url = url;
		}
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

	public static void addPackCard(ModPack modPack, int id){
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
			if(imageMap.containsKey(id)){
				image = imageMap.get(id);
			} else {
				new ImageDownloadItem(packCardController, modPack.iconUrl);
			}
			if(image != null){
				packCardController.modpackImage.setImage(image);
			}

		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}
}
