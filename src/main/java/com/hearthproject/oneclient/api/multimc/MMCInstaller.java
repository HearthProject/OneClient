package com.hearthproject.oneclient.api.multimc;

import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModInstaller;
import com.hearthproject.oneclient.api.PackType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class MMCInstaller extends ModInstaller {
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
			e.printStackTrace();
		}
	}

	@Override
	public PackType getType() {
		return PackType.MULTIMC;
	}
}
