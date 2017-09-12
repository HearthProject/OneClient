package com.hearthproject.oneclient.api.curse;

import com.hearthproject.oneclient.json.JsonUtil;

public class FileData {
	public int projectID;
	public int fileID;
	public boolean required;

	public FileData(int projectID, int fileID) {
		this.projectID = projectID;
		this.fileID = fileID;
	}

	public String getURL() {

		CurseProject.CurseFile file = JsonUtil.read(Curse.getFileURL(projectID, fileID), CurseProject.CurseFile.class);
		return file.getDownloadURL();
	}

	@Override
	public String toString() {
		return projectID + "/" + fileID;
	}
}
