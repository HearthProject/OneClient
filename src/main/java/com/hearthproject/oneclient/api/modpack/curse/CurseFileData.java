package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.api.base.FileData;
import com.hearthproject.oneclient.api.cmdb.Database;

public class CurseFileData extends FileData {
    private int projectID, fileID;

    public CurseFileData(int projectID) {
        this.projectID = projectID;
    }


    public CurseFileData(int projectID, int fileID) {
        this.projectID = projectID;
        this.fileID = fileID;
    }

    public CurseFileData(Database.ProjectFile project) {
        setProjectFile(project);
    }

    public void setProjectFile(Database.ProjectFile project) {
        this.projectID = project.getProject();
        this.fileID = project.getId();
    }

    public Database.ProjectFile getProjectFile() {
        return Curse.getDatabase().getFile(fileID);
    }

    public Database.Project getProject() {
        return Curse.getDatabase().getProject(projectID);
    }

    public int getProjectID() {
        return projectID;
    }

    public int getFileID() {
        return fileID;
    }
}