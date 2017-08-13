package com.hearthproject.oneclient.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

}
