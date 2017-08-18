package com.hearthproject.oneclient.util.curse;

import com.hearthproject.oneclient.fx.SplashScreen;
import com.hearthproject.oneclient.json.JsonUtil;

import java.io.IOException;

import com.hearthproject.oneclient.json.models.curse.CursePacks;
import org.apache.commons.io.IOUtils;

import java.net.URL;

public class CursePackUtil {

	public static CursePacks packs;

	public static CursePacks loadModPacks() throws IOException {
		if(packs != null){
			return packs;
		}
		SplashScreen.updateProgess("Downloading curse modpacks.json file", 60);
		String jsonStr = IOUtils.toString(new URL("https://github.com/NikkyAI/alpacka-meta-files/raw/master/modpacks.json"));
		SplashScreen.updateProgess("Reading modpacks", 80);
		return packs = JsonUtil.GSON.fromJson(jsonStr, CursePacks.class);
	}

}
