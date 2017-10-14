package com.hearthproject.oneclient;

import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.logging.log4j.ThreadContext;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;

public class Constants {

    public static boolean PORTABLE;

    public static File RUN_DIR = null;
    public static File TEMPDIR;
    public static File INSTANCEDIR;
    public static File LOGFILE;
    public static File ICONDIR;
    public static File MINECRAFTDIR;
    public static File EXPORTS;

    public static final String LIBRARIES_BASE = "https://libraries.minecraft.net/";
    public static final String RESOURCES_BASE = "http://resources.download.minecraft.net/";
    public static final String MAVEN_CENTRAL_BASE = "http://central.maven.org/maven2/";

    public static void setupDirs() {
        System.setProperty("http.agent", "OneClient/1.0");
        TEMPDIR = FileUtil.findDirectory(getRunDir(), "temp");
        INSTANCEDIR = FileUtil.findDirectory(getRunDir(), "instances");
        LOGFILE = FileUtil.findDirectory(getRunDir(), "logs");
        ICONDIR = FileUtil.findDirectory(Constants.TEMPDIR, "images");
        MINECRAFTDIR = FileUtil.findDirectory(getRunDir(), "minecraft");
        EXPORTS = FileUtil.findDirectory(getRunDir(), "exports");
        ThreadContext.put("logs", LOGFILE.toString());
        OneClientLogging.init();
    }

    public static File getRunDir() {
        return RUN_DIR;
    }

    public static String getVersion() {
        return Constants.class.getPackage().getImplementationVersion();
    }

    public static File getDefaultDir() {
        return new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "OneClient");
    }

    public static File getInstallConfig() {
        return new File(OperatingSystem.getApplicationDataDirectory(), "settings.json");
    }

    public static StaticSettings getSettings() throws IOException {
        File config = getInstallConfig();
        if (config.exists()) {
            return JsonUtil.read(config, StaticSettings.class);
        }
        return null;
    }

    public static void saveSettings(StaticSettings staticSettings) throws IOException {
        JsonUtil.save(getInstallConfig(), JsonUtil.GSON.toJson(staticSettings));
    }

    public static class StaticSettings {
        private String installLocation;

        public StaticSettings(String installLocation) {
            this.installLocation = installLocation;
        }

        public StaticSettings() {
        }

        public File getInstallLocation() {
            return new File(installLocation);
        }


    }

}