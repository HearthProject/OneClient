package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;

import java.io.*;
import java.lang.reflect.Field;

public class SettingsUtil {
    public static File settingsFile;
    public static LauncherSettings settings;

    public static void init() {
        settingsFile = new File(Constants.getRunDir(), "settings.json");
        reloadSettings();
    }

    public static void reloadSettings() {
        if (!settingsFile.exists()) {
            try (Writer writer = new FileWriter(settingsFile)) {
                JsonUtil.GSON.toJson(new LauncherSettings(), writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try (Reader reader = new FileReader(settingsFile)) {
            settings = JsonUtil.GSON.fromJson(reader, LauncherSettings.class);
            if (settings == null) {
                try (Writer writer = new FileWriter(settingsFile)) {
                    JsonUtil.GSON.toJson(new LauncherSettings(), writer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                reloadSettings();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateSettingsFile() {
        try (Writer writer = new FileWriter(settingsFile)) {
            JsonUtil.GSON.toJson(settings, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateSetting(String fieldName, Object value) {
        for (Field field : settings.getClass().getDeclaredFields()) {
            if (field.getType() == LauncherSettings.Setting.class)
                if (field.getName().equals(fieldName)) {
                    try {
                        LauncherSettings s = new LauncherSettings();
                        s.getClass().getDeclaredField(fieldName).set(settings, new LauncherSettings.Setting(value, ((LauncherSettings.Setting) field.get(settings)).name));
                        settings = s;
                        updateSettingsFile();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
