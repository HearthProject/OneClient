package com.hearthproject.oneclient.util.curse;

import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.curse.ModPacks;
import com.hearthproject.oneclient.json.models.forge.ForgeVersions;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CurseUtil {

	public static ModPacks loadModPacks() throws IOException {
		String jsonStr = IOUtils.toString(new URL("https://github.com/NikkyAI/alpacka-meta-files/raw/master/modpacks.json"));
		return  JsonUtil.GSON.fromJson(jsonStr, ModPacks.class);
	}


}
