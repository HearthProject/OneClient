package com.hearthproject.oneclient.util.curse;

import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.curse.ModPacks;
import com.hearthproject.oneclient.json.models.forge.ForgeVersions;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CurseUtil {

	public static ModPacks packs;

	public static StandardAnalyzer analyzer = new StandardAnalyzer();
	public static Directory index = new RAMDirectory();
	public static IndexWriterConfig config = new IndexWriterConfig(analyzer);
	public static IndexWriter indexWriter;

	public static ModPacks loadModPacks() throws IOException {
		if(packs != null){
			return packs;
		}
		String jsonStr = IOUtils.toString(new URL("https://github.com/NikkyAI/alpacka-meta-files/raw/master/modpacks.json"));
		return packs = JsonUtil.GSON.fromJson(jsonStr, ModPacks.class);
	}

	public static void buildPackIndex() throws IOException {
		indexWriter = new IndexWriter(index, config);
		for(ModPacks.CursePack pack : loadModPacks().Data){
			addPack(indexWriter, pack);
		}
		indexWriter.close();
	}

	private static void addPack(IndexWriter w, ModPacks.CursePack pack) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("title", pack.Name, Field.Store.YES));
		doc.add(new StringField("summary", pack.Summary, Field.Store.YES));
		doc.add(new TextField ("id", Integer.toString(pack.Id), Field.Store.YES));
		w.addDocument(doc);
	}


}
