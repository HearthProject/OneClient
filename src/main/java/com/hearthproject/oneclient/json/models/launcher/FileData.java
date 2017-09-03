package com.hearthproject.oneclient.json.models.launcher;

public class FileData {
	public int projectID;
	public int fileID;
	public boolean required;

	public FileData(int projectID, int fileID) {
		this.projectID = projectID;
		this.fileID = fileID;
	}

	@Override
	public String toString() {
		return projectID + "/" + fileID;
	}
}
