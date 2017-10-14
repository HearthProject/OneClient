package com.hearthproject.oneclient.api.modpack.multimc;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModpackInstaller;
import com.hearthproject.oneclient.api.base.PackType;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class MMCInstaller extends ModpackInstaller {
	private File dir;

	public MMCInstaller(File dir) {
		super(PackType.MULTIMC);
		this.dir = dir;
	}

	@Override
	public void install(Instance instance) {
		try {
			FileUtils.copyDirectory(new File(dir, "minecraft"), instance.getDirectory());
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
	}

}
