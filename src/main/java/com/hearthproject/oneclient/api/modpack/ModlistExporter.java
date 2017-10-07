package com.hearthproject.oneclient.api.modpack;

import com.hearthproject.oneclient.Constants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ModlistExporter implements IExporter {

	@Override
	public void export(Instance instance) {
		instance.verifyMods();
		new Thread(() -> {
			StringBuilder builder = new StringBuilder();
			for (ModInstaller mod : instance.getMods().sorted()) {
				builder.append(mod.getName()).append("\n");
			}
			try {
				FileUtils.writeStringToFile(new File(Constants.EXPORTS, instance.getName().toLowerCase() + "-mods.txt"), builder.toString(), Charset.defaultCharset());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
