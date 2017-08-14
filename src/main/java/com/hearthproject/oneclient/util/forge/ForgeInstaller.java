package com.hearthproject.oneclient.util.forge;

import net.minecraftforge.installer.ClientInstall;
import net.minecraftforge.installer.VersionInfo;

import java.io.File;

public class ForgeInstaller {

	protected static void installForge(File installDir) {
		VersionInfo.INSTANCE = new VersionInfo();
		new ClientInstall().run(installDir, input -> false);
	}

}
