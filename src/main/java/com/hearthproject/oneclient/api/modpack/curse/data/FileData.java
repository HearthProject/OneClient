package com.hearthproject.oneclient.api.modpack.curse.data;

public class FileData {
    public String projectID;
    public String fileID;
    public boolean required;

    public FileData(String projectID, String fileID) {
        this.projectID = projectID;
        this.fileID = fileID;
    }

//    public Database.ProjectFile getCurseFile() {
//        return JsonUtil.read(Curse.getFileURL(projectID, fileID), Database.ProjectFile.class);
//    }
//
//    public String getURL() {
//        if (getCurseFile() != null) {
//            return getCurseFile().getDownloadURL();
//        }
//        return null;
//    }

    @Override
    public String toString() {
        return projectID + "/" + fileID;
    }
}
