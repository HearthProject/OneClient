package com.hearthproject.oneclient.util.forge;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.util.ClasspathUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ForgeUtils {

	//1.12-14.21.1.2443
	public static void installForge(File file, String forgeVersion) throws IOException {
		//http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12-14.21.1.2443/forge-1.12-14.21.1.2443-installer.jar
		URL forgeInstallerURL = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + forgeVersion + "/forge-" + forgeVersion + "-installer.jar");
		File forgeInstaller = new File(Constants.TEMPDIR, "forge-installer-" + forgeVersion + ".jar");
		if (!forgeInstaller.exists()) {
			FileUtils.copyURLToFile(forgeInstallerURL, forgeInstaller);
		}
		ClasspathUtils.addFileToCP(forgeInstaller);
		ForgeInstaller.installForge(file);
	}

}
