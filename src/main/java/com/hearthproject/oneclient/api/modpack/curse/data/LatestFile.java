package com.hearthproject.oneclient.api.modpack.curse.data;

import com.hearthproject.oneclient.api.modpack.curse.Curse;

public class LatestFile {
	public String FileType;
	private String GameVesion;
	public String ProjectFileID;

	public String getGameVersion() {
		return Curse.formatVersion(GameVesion);
	}

}