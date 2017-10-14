package com.hearthproject.oneclient.api.cmdb;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.scene.control.Hyperlink;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Database {
    public static final Set<String> WILDCARDS = Sets.newHashSet("*", "All");
    public Map<String, String> authors;
    public Map<Integer, Project> projects;
    public Map<Integer, Category> categories;
    public Map<Integer, ProjectFile> files;

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

    public List<Project> searchProjects(String query, String type, String version, String sort, boolean reverse) {
        return searchProjects(query, type, -1, 80, version, sort, reverse);
    }


    public List<Project> searchProjects(String query, String type, String sort) {
        return searchProjects(query, type, -1, 80, "*", sort, false);
    }

    public List<Project> searchProjects(String query, String type, int limit, int threshold, String version, String sort, boolean reverse) {
        List<Pair<Project, Integer>> out = Lists.newArrayList();
        for (Project project : projects.values()) {
            if (!isWildcard(type) && !project.type.equals(type))
                continue;
            if (!isWildcard(version) && !project.versions.contains(version))
                continue;
            if (!query.isEmpty()) {
                int partial = FuzzySearch.partialRatio(query.toLowerCase(), project.title.toLowerCase());
                int full = FuzzySearch.ratio(query.toLowerCase(), project.title.toLowerCase());

                int body = FuzzySearch.partialRatio(query.toLowerCase(), project.desc.toLowerCase());
                int full_body = FuzzySearch.ratio(query.toLowerCase(), project.desc.toLowerCase());

                if (partial >= threshold) {
                    out.add(Pair.of(project, partial + full));
                    if (limit > 0 && out.size() >= limit)
                        break;
                    continue;
                }
                if (body >= threshold) {
                    out.add(Pair.of(project, body + full_body));
                    if (limit > 0 && out.size() >= limit)
                        break;
                }
            } else {
                out.add(Pair.of(project, 0));
            }
        }
        out.sort(Comparator.comparingInt(Pair::getValue));
        Collections.reverse(out);
        Comparator<Project> sorting = sort(sort, true);
        if (sorting != null)
            return out.stream().map(Pair::getKey).sorted(sort(sort, reverse)).collect(Collectors.toList());
        return out.stream().map(Pair::getKey).collect(Collectors.toList());
    }

    public ProjectFile searchFiles(String filename) {
        return files.values().stream().filter(f -> f.filename.equals(filename.toLowerCase())).findAny().orElse(null);
    }

    public List<Integer> getProjectFiles(int pid, String version) {
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

    public List<ProjectFile> getFiles(List<Integer> ids) {
        return ids.stream().map(id -> files.get(id)).sorted(Comparator.comparingInt(ProjectFile::getDate).reversed()).collect(Collectors.toList());
    }

    public static final String[] TYPES = new String[]{"mod", "texturepack", "world", "modpack"};

    private boolean isWildcard(String query) {
        return WILDCARDS.contains(query);
    }

    private Comparator<Project> sort(String sorting, boolean reverse) {
        Comparator<Project> comparator;
        switch (sorting.toLowerCase()) {
            default:
            case "popularity":
                comparator = Comparator.comparing(Project::getPopularity).reversed();
                break;
            case "alphabetical":
                comparator = Comparator.comparing(Project::getTitle).reversed();
                break;
            case "fuzzy":
                return null;
        }
        if (comparator == null)
            return null;
        return reverse ? comparator.reversed() : comparator;
    }

    public class Project implements Comparable<Project> {
        private boolean featured;
        private double popularity;
        private int id;
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
        private List<Integer> files;
        private List<Attachment> attachments;
        private Set<String> versions;

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

        public int getId() {
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

        public List<ProjectFile> getProjectFiles() {
            return Curse.getDatabase().getFiles(getFiles());
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
