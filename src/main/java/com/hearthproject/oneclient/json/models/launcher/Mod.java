package com.hearthproject.oneclient.json.models.launcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.MiscUtil;
import javafx.beans.property.SimpleStringProperty;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

@SuppressWarnings("unused")
public class Mod {
	public final File file;
	public final SimpleStringProperty name;
	public final MiscUtil.ProviderProperty<Boolean> enabled;

	public Mod(File file) {
		String fileName = file.getName();
		this.file = file;
		JsonObject mcmodinfo = readMCModInfo(file);
		if (mcmodinfo != null && mcmodinfo.has("name")) {
			this.name = new SimpleStringProperty(mcmodinfo.get("name").getAsString());
		} else {
			this.name = new SimpleStringProperty(FilenameUtils.removeExtension(fileName));
		}
		this.enabled = new MiscUtil.ProviderProperty<>(() -> !FilenameUtils.isExtension(file.toString(), ".disabled"));
	}

	public String getName() {
		return name.get();
	}

	public SimpleStringProperty nameProperty() {
		return name;
	}

	public MiscUtil.ProviderProperty<Boolean> enabledProperty() {
		return enabled;
	}

	private JsonObject readMCModInfo(File file) {
		try {
			ZipFile zip = new ZipFile(file);
			zip.extractFile("mcmod.info", Constants.TEMPDIR.toString());
			File info = new File(Constants.TEMPDIR, "mcmod.info");
			if (info.exists()) {
				JsonArray array = null;
				try {
					JsonObject o = JsonUtil.read(info, JsonObject.class);
					array = o.get("modList").getAsJsonArray();
				} catch (JsonSyntaxException ignore) {}
				try {
					if (array == null)
						array = JsonUtil.read(info, JsonArray.class);
					return array.get(0).getAsJsonObject();
				} catch (JsonSyntaxException ignore) { }
			}

		} catch (ZipException ignore) { }
		return null;
	}

}