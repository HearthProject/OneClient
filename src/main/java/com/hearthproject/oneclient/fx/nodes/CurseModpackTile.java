package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.api.modpack.curse.CurseDownloader;
import com.hearthproject.oneclient.api.modpack.curse.CurseFileData;
import com.hearthproject.oneclient.util.MiscUtil;

public class CurseModpackTile extends ModpackTile {
    public CurseModpackTile(Instance instance, CurseDownloader installer) {
        super(instance, installer);
        CurseFileData data = installer.getData();
        setImage(data.getProject().getIconURL(), data.getProject().getTitle());
        setTitle(data.getProject().getTitle(), Curse.getCurseForge(data.getProjectID()));
        setFiles(data.getProject().getProjectFiles());
        data.setProjectFile(comboFile.getValue());
        comboFile.valueProperty().addListener((v, a, b) -> data.setProjectFile(comboFile.getValue()));
        Database.Project project = data.getProject();
        addRightInfo("Downloads: %s", MiscUtil.formatNumbers(project.getDownloads()));
        addRightInfo("Versions: %s", project.getVersions());
        addLeftInfo("By %s", project.getAuthors());
        addLeftInfo("%s", project.getDesc());
    }
}
