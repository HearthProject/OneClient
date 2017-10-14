package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModpackInstaller;
import com.hearthproject.oneclient.api.base.PackType;
import com.hearthproject.oneclient.api.modpack.DownloadManager;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.hearthproject.oneclient.util.MiscUtil.checkCancel;

public class CurseDownloader extends ModpackInstaller {
    private transient Manifest manifest;

    public CurseDownloader(int projectID) {
        super(PackType.CURSE);
        this.data = new CurseFileData(projectID);
    }

    @Override
    public void install(Instance instance) {
        if (checkCancel())
            return;
        if (getData().getFileID() == 0) {
            OneClientLogging.error(new NullPointerException("No Curse File Selected"));
            return;
        }
        if (checkCancel())
            return;
        if (checkCancel())
            return;
        //TODO more precise
        if (instance.getModDirectory().exists()) {
            try {
                FileUtils.deleteDirectory(instance.getModDirectory());
            } catch (IOException e) {
                OneClientLogging.error(e);
            }
        }
        if (checkCancel())
            return;
        //TODO more precise
        if (instance.getConfigDirectory().exists()) {
            try {
                FileUtils.deleteDirectory(instance.getConfigDirectory());
            } catch (IOException e) {
                OneClientLogging.error(e);
            }
        }
        try {
            //Resolve Icon
            ImageUtil.downloadAndOpenImage(getData().getProject().getIconURL(), instance.getName());
            FileUtils.copyFile(new File(Constants.ICONDIR, FileUtil.encode(instance.getName()) + ".png"), new File(instance.getDirectory(), "icon.png"));
        } catch (IOException e) {
            OneClientLogging.error(e);
        }

        if (checkCancel())
            return;
        DownloadManager.updateMessage(instance.getName(), "Downloading %s", instance.getName());
        if (checkCancel())
            return;
        File directory = FileUtil.findDirectory(Constants.TEMPDIR, instance.getName());
        File pack = FileUtil.extractFromURL(getData().getProjectFile().getUrl(), directory);
        DownloadManager.updateMessage(instance.getName(), "Extracting %s", instance.getName());
        if (checkCancel())
            return;
        manifest = JsonUtil.read(new File(pack, "manifest.json"), Manifest.class);
        DownloadManager.updateMessage(instance.getName(), "Installing %s", instance.getName());
        if (checkCancel())
            return;
        new CurseInstaller(manifest, pack).install(instance);
    }

    @Override
    public void update(Instance instance) {
        //TODO
    }

    @Override
    public CurseFileData getData() {
        return (CurseFileData) super.getData();
    }
}
