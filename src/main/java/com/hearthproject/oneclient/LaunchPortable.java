package com.hearthproject.oneclient;

import java.io.IOException;

public class LaunchPortable {

	public static void main(String[] args) throws IOException {
		Constants.PORTABLE = true;
		Launch.main(args);
	}

}
