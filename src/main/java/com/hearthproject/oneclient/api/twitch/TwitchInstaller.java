package com.hearthproject.oneclient.api.twitch;

import com.hearthproject.oneclient.api.Instance;
import com.hearthproject.oneclient.api.ModpackInstaller;
import com.hearthproject.oneclient.api.PackType;
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
			e.printStackTrace();
		}
		super.install(instance);
	}
}
