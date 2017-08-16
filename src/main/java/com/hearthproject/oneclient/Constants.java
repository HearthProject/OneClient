package com.hearthproject.oneclient;

import java.io.File;

public class Constants {

    public static final File RUNDIR = new File("./run");
    public static final File TEMPDIR = new File(RUNDIR, "temp");
    public static final File INSTANCEDIR = new File(RUNDIR, "instances");
    public static final File LOGFILE = new File(RUNDIR, "log.txt");

    public static final String LIBRARIES_BASE = "https://libraries.minecraft.net/";
    public static final String RESOURCES_BASE = "http://resources.download.minecraft.net/";

}