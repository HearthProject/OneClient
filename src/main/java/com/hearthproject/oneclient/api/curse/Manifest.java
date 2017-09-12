package com.hearthproject.oneclient.api.curse;

import com.google.common.collect.Lists;

import java.util.List;

public class Manifest {

	public String manifestType;
	public String manifestVersion;
	public String name;
	public String version;
	public String author;
	public int projectID;
	public List<FileData> files;
	public String overrides;
	public String icon;

	public Manifest() {
		files = Lists.newArrayList();
	}

}
