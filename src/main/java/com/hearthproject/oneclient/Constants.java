package com.hearthproject.oneclient;

import java.io.File;

public class Constants {

    public static final File TEMPDIR = new File(getRunDir(), "temp");
    public static final File INSTANCEDIR = new File(getRunDir(), "instances");
    public static final File LOGFILE = new File(getRunDir(), "log.txt");

    public static final String LIBRARIES_BASE = "https://libraries.minecraft.net/";
    public static final String RESOURCES_BASE = "http://resources.download.minecraft.net/";

    public static boolean CUSTOM_RUN = false;


    public static File getRunDir(){
    	String runDir = System.getProperty("OneClient.runDir", "");
    	if(!runDir.isEmpty()){
		    CUSTOM_RUN = true;
    		return new File(runDir);
	    }
    	return new File(System.getProperty("user.home") + System.getProperty("path.separator") + "OneCLient");
    }

}