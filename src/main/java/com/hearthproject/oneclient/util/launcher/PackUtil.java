package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.ModPack;
import com.hearthproject.oneclient.json.models.launcher.ModPackList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class PackUtil {

	public static ModPackList packs;

	public static StandardAnalyzer analyzer = new StandardAnalyzer();
	public static Directory index = new RAMDirectory();
	public static IndexWriterConfig config = new IndexWriterConfig(analyzer);
	public static IndexWriter indexWriter;

	public static ModPackList loadModPacks() throws IOException {
		if(packs != null){
			return packs;
		}
		SplashScreen.updateProgess("Downloading modpacks.json file", 40);
		String jsonStr = IOUtils.toString(new URL("http://hearthproject.uk/files/modpacks.json"));
		SplashScreen.updateProgess("Reading modpacks", 80);
		return packs = JsonUtil.GSON.fromJson(jsonStr, ModPackList.class);
	}

	public static void buildPackIndex() throws IOException {
		SplashScreen.updateProgess("Building modpack instances", 80);
		indexWriter = new IndexWriter(index, config);
		int i = 1;
		for(ModPack pack : packs.packs){
			SplashScreen.updateProgess("Indexing " + pack.name, 80 + (packs.packs.size() / i++) * 20);
			addPack(indexWriter, pack);
		}
		indexWriter.close();
	}

	private static void addPack(IndexWriter w, ModPack pack) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("title", pack.name, Field.Store.YES));
		doc.add(new StringField("summary", pack.description, Field.Store.YES));
		w.addDocument(doc);
	}

	public static void main(String[] args) throws IOException {
		ModPack modPack = new ModPack("Test pack 1", "modmuss50", "this is a pack used for testing the launcher", "http://via.placeholder.com/200x200");
		ModPack modPack2 = new ModPack("Test pack 2", "modmuss50", "this is a pack used for testing the launcher", "http://via.placeholder.com/200x200");

		ModPackList modPackList = new ModPackList();
		modPackList.packs.add(modPack);
		modPackList.packs.add(modPack2);
		FileUtils.write(new File("modpacks.json"), JsonUtil.GSON.toJson(modPackList));
	}


}
