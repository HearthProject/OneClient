package com.hearthproject.oneclient.api.modpack.curse;

import com.google.common.collect.Lists;

import java.util.List;

public class Manifest {

    public int manifestVersion;
    public int projectID;
    public String manifestType;
    public String name;
    public String version;
    public String author;
    public String overrides;
    public String icon;
    public List<Project> files;
    public Minecraft minecraft;

    public Manifest() {
        files = Lists.newArrayList();
    }

    public static class Minecraft {
        public String version;
        public List<Modloader> modLoaders = Lists.newArrayList();

        public static class Modloader {
            public String id;
            public boolean primary;

            public Modloader(String forgeVersion) {
                this.id = "forge-" + forgeVersion;
                this.primary = true;
            }
        }

        public String getModloader() {
            for (Modloader modloader : modLoaders) {
                if (modloader.id != null) {
                    return modloader.id.replace("forge-", "");
                }
            }
            return "";
        }
    }

    public static class Project {
        public int projectID;
        public int fileID;
        public boolean required;

        public Project(int projectID, int fileID) {
            this.projectID = projectID;
            this.fileID = fileID;
        }

        @Override
        public String toString() {
            return projectID + "/" + fileID;
        }
    }

}
