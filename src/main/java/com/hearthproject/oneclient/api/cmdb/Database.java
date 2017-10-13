package com.hearthproject.oneclient.api.cmdb;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.scene.control.Hyperlink;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Database {
    public static final String WILDCARD = "*";
    public Map<String, String> authors;
    public Map<Integer, Project> projects;
    public Map<Integer, Category> categories;
    public Map<Integer, ProjectFile> files;
    public Map<String, List<Project>> popular;

    public Database() {
    }

    public Project getProject(int pid) {
        return projects.getOrDefault(pid, null);
    }

    public ProjectFile getFile(int pid) {
        return files.getOrDefault(pid, null);
    }

    public Category getCategory(int pid) {
        return categories.getOrDefault(pid, null);
    }

    public List<Project> searchProjects(String query, String type) {
        return searchProjects(query, type, 25, 80, WILDCARD);
    }

    public List<Project> searchProjects(String query, String type, int limit, int threshold, String version) {
        List<Pair<Project, Integer>> out = Lists.newArrayList();
        for (Project project : projects.values()) {
            if (!type.equals(WILDCARD) && !project.type.equals(type))
                continue;
            if (!version.equals(WILDCARD) && !project.versions.contains(version))
                continue;

            int partial = FuzzySearch.partialRatio(query.toLowerCase(), project.title.toLowerCase());
            int full = FuzzySearch.ratio(query.toLowerCase(), project.title.toLowerCase());

            int body = FuzzySearch.partialRatio(query.toLowerCase(), project.desc.toLowerCase());
            int full_body = FuzzySearch.ratio(query.toLowerCase(), project.desc.toLowerCase());

            if (partial >= threshold) {
                out.add(Pair.of(project, partial + full));
                if (out.size() >= limit)
                    break;
                continue;
            }
            if (body >= threshold) {
                out.add(Pair.of(project, body + full_body));
                if (out.size() >= limit)
                    break;
            }
        }
        out.sort(Comparator.comparingInt(Pair::getValue));
        Collections.reverse(out);
        return out.stream().map(Pair::getKey).collect(Collectors.toList());
    }

    public ProjectFile searchFiles(String filename) {
        return files.values().stream().filter(f -> f.filename.equals(filename.toLowerCase())).findAny().orElse(null);
    }

    public List<Integer> getFiles(int pid, String version) {
        Project project = getProject(pid);
        if (project == null)
            return null;
        List<Integer> files = project.files;
        List<Integer> out = Lists.newArrayList();
        for (int file : files) {
            if (getFile(file).versions.contains(version))
                out.add(file);
        }
        return out;
    }

    public List<Project> getPopular(String type) {
        return getPopular(type, -1, WILDCARD);
    }

    public List<Project> getPopular(String type, int limit, String version) {
        List<Project> projects = this.popular.get(type);
        if (!Objects.equals(version, WILDCARD))
            projects = projects.stream().filter(p -> p.versions.contains(version)).collect(Collectors.toList());
        if (limit > 0)
            projects = projects.subList(0, limit);
        return projects;
    }

    public static final String[] TYPES = new String[]{"mod", "texturepack", "world", "modpack"};

    public void generatePopular() {
        if (this.projects != null) {
            this.popular = Maps.newHashMap();
            for (String type : TYPES) {
                List<Project> projects = this.projects.values().stream().filter(p -> p.type.equals(type)).sorted().collect(Collectors.toList());
                this.popular.put(type, projects);
            }
        }
    }

    public class Project implements Comparable<Project> {
        private boolean featured;
        private double popularity;
        private int rank;
        private int downloads;
        private int primaryCategory;
        private int[] categories;
        private String type;
        private String stage; //release, beta , alpha
        private String defaultFile;
        private String site;
        private String desc;
        private String title;
        private String primaryAuthor;
        private List<String> authors;
        private String id;
        private List<Integer> files;
        private Set<String> versions;
        private List<Attachment> attachments;

        @Override
        public String toString() {
            return JsonUtil.GSON.toJson(this);
        }

        public boolean isFeatured() {
            return featured;
        }

        public double getPopularity() {
            return popularity;
        }

        public int getRank() {
            return rank;
        }

        public int getDownloads() {
            return downloads;
        }

        public int getPrimaryCategory() {
            return primaryCategory;
        }

        public int[] getCategories() {
            return categories;
        }

        public String getType() {
            return type;
        }

        public String getStage() {
            return stage;
        }

        public String getDefaultFile() {
            return defaultFile;
        }

        public String getSite() {
            return site;
        }

        public String getDesc() {
            return desc;
        }

        public String getTitle() {
            return title;
        }

        public String getPrimaryAuthor() {
            return primaryAuthor;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public String getId() {
            return id;
        }

        public List<Integer> getFiles() {
            return files;
        }

        public Set<String> getVersions() {
            return versions;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public String getIconURL() {
            return getAttachments().stream().findFirst().map(a -> a.url).orElse(null);
        }

        @Override
        public int compareTo(Project project) {
            return Double.compare(project.popularity, popularity);
        }
    }

    private class Attachment {
        @SerializedName("default")
        private boolean isDefault;
        private String desc;
        private String thumbnail;
        private String name;
        private String url;

        @Override
        public String toString() {
            return JsonUtil.GSON.toJson(this);
        }
    }


    public class ProjectFile {
        private int id;
        private int project;
        private int date;
        private Dependency[] dependencies;
        private String fingerprint;
        private String type;
        private String name;
        private String filename;
        private String url;
        private Set<String> versions;
        private boolean availabe;
        private boolean alternate;
        private int alternateFile;

        @Override
        public String toString() {
            return getFilename();
        }

        public class Dependency {
            @SerializedName("AddOnId")
            private int id;
            @SerializedName("Type")
            private String Type; //optional, required
        }

        public int getId() {
            return id;
        }

        public int getProject() {
            return project;
        }

        public int getDate() {
            return date;
        }

        public Dependency[] getDependencies() {
            return dependencies;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getFilename() {
            return filename;
        }

        public Set<String> getVersions() {
            return versions;
        }

        public boolean isAvailabe() {
            return availabe;
        }

        public boolean isAlternate() {
            return alternate;
        }

        public int getAlternateFile() {
            return alternateFile;
        }

        public String getUrl() {
            return url;
        }
    }

    public class Category {
        private String title;
        private String id;
        private String url;

        @Override
        public String toString() {
            return JsonUtil.GSON.toJson(this);
        }

        //TODO category image views
        public Hyperlink getNode() {
            Hyperlink link = new Hyperlink(title);
            link.setOnAction(event -> OperatingSystem.browseURI(url));
            return link;
        }
    }
}
