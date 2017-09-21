package com.hearthproject.oneclient.api.curse.data;

import com.hearthproject.oneclient.api.curse.Curse;
import com.hearthproject.oneclient.json.JsonUtil;

public class FileData {
	public String projectID;
	public String fileID;
	public boolean required;

	public FileData(String projectID, String fileID) {
		this.projectID = projectID;
		this.fileID = fileID;
	}

	public String getURL() {

		CurseFullProject.CurseFile file = JsonUtil.read(Curse.getFileURL(projectID, fileID), CurseFullProject.CurseFile.class);
		if (file != null) {
			return file.getDownloadURL();
		}
		return null;
	}

	@Override
	public String toString() {
		return projectID + "/" + fileID;
	}
}
