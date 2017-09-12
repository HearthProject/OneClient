package com.hearthproject.oneclient.api;

import com.hearthproject.oneclient.api.curse.CurseImporter;

public class Testing {
	public static void main(String[] args) {

		System.setProperty("http.agent", "OneClient/1.0");

		HearthInstance instance = new CurseImporter("244939", "1.7.10").create();

		if (instance != null)
			instance.install();
	}
}
