package com.hearthproject.oneclient.json.models.modloader;

public interface IModloader {

	class None implements IModloader {
		@Override
		public String toString() {
			return "None";
		}
	}

}
