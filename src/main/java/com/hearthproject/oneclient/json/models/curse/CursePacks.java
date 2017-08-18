package com.hearthproject.oneclient.json.models.curse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CursePacks {
	public long timestamp;
	@SerializedName("Data")
	public List<CursePack> packs;

	public class CursePack {
		@SerializedName("Attachments")
		public List<CurseAttachments> attachments;

		@SerializedName("Name")
		public String name;

		@SerializedName("PackageType")
		public String packageType;

		@SerializedName("PrimaryAuthorName")
		public String authors;

		@SerializedName("Summary")
		public String description;

		@SerializedName("WebSiteURL")
		public String webSiteURL;

		@SerializedName("IconId")
		public int iconId;

		@SerializedName("Id")
		public int id;
	}

	public class CurseAttachments {
		@SerializedName("Description")
		public String description;

		@SerializedName("IsDefault")
		public boolean isDefault;

		@SerializedName("ThumbnailUrl")
		public String thumbnailUrl;

		@SerializedName("Title")
		public String title;

		@SerializedName("Url")
		public String url;
	}
}
