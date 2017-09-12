package com.hearthproject.oneclient.api.multimc;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.HeathInstance;
import com.hearthproject.oneclient.api.IImporter;
import com.hearthproject.oneclient.util.files.FileUtil;
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
			e.printStackTrace();
		}
	}

	@Override
	public HeathInstance create() {
		String name = config.getProperty("name");
		String gameVersion = config.getProperty("IntendedVersion");

		HeathInstance instance = new HeathInstance(name, "", gameVersion, "", new MMCInstaller(pack));
		instance.setForgeVersion(config.getProperty("ForgeVersion"));
		return instance;
	}
}
