package com.hearthproject.oneclient.util.launcher;

public class LauncherSettings {
    public Setting left_align_window_buttons = new Setting(Boolean.FALSE, "Left-Align Window Buttons");
    public Setting show_console_window = new Setting(Boolean.FALSE, "Left-Align Window Buttons");

    public static class Setting {
        public String name;
        public Object setting;

        public Setting(Object setting, String name) {
            this.name = name;
            this.setting = setting;
        }
    }
}
