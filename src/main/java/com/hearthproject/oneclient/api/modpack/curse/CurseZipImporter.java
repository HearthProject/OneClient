package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.modpack.IImporter;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class CurseZipImporter implements IImporter {

	private File curseZip, pack;

	public CurseZipImporter(File curseZip) {
		this.curseZip = curseZip;
		this.pack = FileUtil.extract(curseZip, new File(Constants.TEMPDIR, FilenameUtils.removeExtension(curseZip.getName())));
	}

	@Override
	public Instance create() {
		Manifest manifest = JsonUtil.read(new File(pack, "manifest.json"), Manifest.class);
		if (manifest != null)
            return new Instance(manifest.name, new CurseInstaller(manifest, pack));
        return null;
	}
}
