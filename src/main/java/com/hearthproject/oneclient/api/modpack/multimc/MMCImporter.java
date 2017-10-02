package com.hearthproject.oneclient.api.modpack.multimc;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.modpack.IImporter;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MMCImporter implements IImporter {

	private File file;
	private File pack;
	private Properties config;

	public MMCImporter(File file) {
		this.file = file;
		pack = FileUtil.getSubdirectory(FileUtil.extract(file, new File(Constants.TEMPDIR, FilenameUtils.removeExtension(file.getName()))), 0);
		config = new Properties();
		try {
			config.load(new FileInputStream(new File(pack, "instance.cfg")));
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
	}

	@Override
	public Instance create() {
		String name = config.getProperty("name");
		String gameVersion = config.getProperty("IntendedVersion");
		Instance instance = new Instance(name, "", new MMCInstaller(pack));
		instance.setGameVersion(gameVersion);
		instance.setForgeVersion(config.getProperty("ForgeVersion"));
		return instance;
	}
}
