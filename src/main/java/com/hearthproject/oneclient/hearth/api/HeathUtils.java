package com.hearthproject.oneclient.hearth.api;

public class HeathUtils {

	public static boolean isPackNameValid(String name){
		return name.matches("[A-Za-z0-9]+");
	}
}
