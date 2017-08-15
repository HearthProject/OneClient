package com.hearthproject.oneclient.json.models.curse;

import java.util.List;

public class ModPacks {
	public long timestamp;
	public List<CursePack>Data;



	public class CursePack {
		public List<CurseAttachments> Attachments;
		public String Name;
		public String PackageType;
		public String PrimaryAuthorName;
		public String Summary;
		public String WebSiteURL;
		public int IconId;
		public int Id;
	}

	public class CurseAttachments {
		public String Description;
		public boolean IsDefault;
		public String ThumbnailUrl;
		public String Title;
		public String Url;
	}

}
