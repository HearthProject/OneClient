package com.hearthproject.oneclient.api.modpack.curse;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Curse {
    private static Cache<String, Database> DATABASE_CACHE;
    private static final String DATABASE_KEY = "MAIN";
    private static final String CURSE_FORGE = "https://minecraft.curseforge.com/projects/${projectID}";
    private static final String CURSE_META_DATA_BASE = "https://openminemods.digitalfishfun.com/raw_cleaned.json.xz";

    public static void init() {
        OneClientLogging.logger.info("Loading Curse Modpacks");
        RemovalListener<String, Object> removalListener = removal -> {
            if (removal.getCause() == RemovalCause.EXPIRED) {
                DATABASE_CACHE.put(DATABASE_KEY, loadDatabase());
            }
        };
        DATABASE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(removalListener).build();
    }

    public static Database getDatabase() {
        if (!DATABASE_CACHE.asMap().containsKey(DATABASE_KEY))
            DATABASE_CACHE.put(DATABASE_KEY, loadDatabase());
        return DATABASE_CACHE.getIfPresent(DATABASE_KEY);
    }

    private static Database loadDatabase() {
        OneClientLogging.info("Curse - Retrieving Database");
        File xz = new File("cmdb.json.xz");
        File json = new File("cmdb.json");
        if (xz.exists())
            FileUtils.deleteQuietly(xz);
        if (json.exists())
            FileUtils.deleteQuietly(json);
        FileUtil.downloadFromURL(CURSE_META_DATA_BASE, xz);
        if (xz.exists()) {
            FileUtil.extract(xz, json);
            if (json.exists()) {
                return JsonUtil.read(json, Database.class);
            }
        }
        return null;
    }

    //Hardcode sub version comparison
    public static String formatVersion(String version) {
        if (version.startsWith("1.12")) {
            version = "1.12";
        }

        if (version.startsWith("1.11")) {
            version = "1.11";
        }
        return version;
    }

    private static boolean isCompatible(String gameVersion, List<String> versions) {
        gameVersion = formatVersion(gameVersion);
        for (String v : versions) {
            if (v.startsWith(gameVersion))
                return true;
        }
        return false;
    }

    public static String getCurseForge(int projectID) {
        return CURSE_FORGE.replace("${projectID}", String.valueOf(projectID));
    }

}
