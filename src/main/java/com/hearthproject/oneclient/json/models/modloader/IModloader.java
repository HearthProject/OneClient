package com.hearthproject.oneclient.json.models.modloader;

public interface IModloader {
	IModloader NONE = new IModloader() {
		@Override
		public String toString() {
			return "None";
		}
	};

	default String getVersion() { return ""; }
}
