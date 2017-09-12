package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.api.multimc.MMCImporter;

import java.io.File;

public class Testing {
	public static void main(String[] args) {

		System.setProperty("http.agent", "OneClient/1.0");

		HearthInstance instance = new MMCImporter(new File("/home/tyler/Pack.zip")).create(); //new CurseImporter("244939","1.7.10").create();

		if (instance != null)
			instance.install();
	}
}
