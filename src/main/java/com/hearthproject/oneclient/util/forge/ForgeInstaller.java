package com.hearthproject.oneclient.util.forge;

import net.minecraftforge.installer.InstallerAction;

import java.io.File;

public class ForgeInstaller {

	protected static void installForge(File installDir) {
		InstallerAction installerAction = InstallerAction.CLIENT;
		installerAction.run(installDir, s -> {
			System.out.println(s);
			return false;
		});
	}

}
