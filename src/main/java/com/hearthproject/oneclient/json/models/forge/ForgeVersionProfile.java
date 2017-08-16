package com.hearthproject.oneclient.json.models.forge;

import java.util.List;

public class ForgeVersionProfile {

	String type, mainClass, inheritsFrom, jar;

	List<Library> libraries;

	public class Library {
		String name;
		boolean serverreq;
		boolean clientreq;
		String url;
		List<String> checksums;
	}
}
