package com.hearthproject.oneclient.api.modpack.twitch;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModpackInstaller;
import com.hearthproject.oneclient.api.base.PackType;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TwitchInstaller extends ModpackInstaller {
	private File twitchFolder;

	public TwitchInstaller() {
		super(PackType.TWITCH);
	}

	public TwitchInstaller(File twitchFolder) {
		this();

		this.twitchFolder = twitchFolder;
	}

	@Override
	public void install(Instance instance) {
		try {
			FileUtils.copyDirectory(twitchFolder, instance.getDirectory());
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
		super.install(instance);
	}
}
