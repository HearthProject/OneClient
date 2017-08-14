package com.hearthproject.oneclient;

import java.io.File;

public class Constants {

	public static File RUNDIR = new File("./run");
	public static File TEMPDIR = new File(RUNDIR, "temp");
	public static File INSTANCEDIR = new File(RUNDIR, "instances");
	public static File LOGFILE = new File(RUNDIR, "log.txt");

	public static final String LIBRARIES_BASE = "https://libraries.minecraft.net/";
	public static final String RESOURCES_BASE = "http://resources.download.minecraft.net/";

}
