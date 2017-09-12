package com.hearthproject.oneclient.util.json.models.modloader;

public interface IModloader {
	IModloader NONE = new IModloader() {
		@Override
		public String toString() {
			return "";
		}
	};

}
