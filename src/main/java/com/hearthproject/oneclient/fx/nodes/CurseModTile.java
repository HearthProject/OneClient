package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.DownloadManager;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.api.modpack.curse.CurseModInstaller;

public class CurseModTile extends ModTile {
    public CurseModTile(Instance instance, CurseModInstaller mod) {
        super(instance, mod);
        Database.Project project = mod.getData().getProject();
        setImage(project.getIconURL(), String.valueOf(project.getId()));
        setTitle(mod.getName(), Curse.getCurseForge(project.getId()));
        addLeftInfo("By %s", project.getAuthors());
        addRightInfo("Versions: %s", project.getVersions());
        addRightInfo("Downloads: %s", mod.getData());
    }

    public static class View extends CurseModTile {
        public View(Instance instance, CurseModInstaller mod) {
            super(instance, mod);

//            Database.ProjectFile file = mod.findLatestUpdate(instance.getGameVersion(),true);
//            if(file != null) {
//                buttonInstall.setVisible(true);
//                buttonInstall.setText("Update");
//                buttonInstall.setOnAction( event -> mod.update(instance, file));
//            }

        }
    }

    public static class Download extends CurseModTile {

        public Download(Instance instance, CurseModInstaller mod) {
            super(instance, mod);
            Database.Project project = mod.getData().getProject();

            if (project.getProjectFiles() != null) {
                setFiles(project.getProjectFiles());
                mod.getData().setProjectFile(comboFile.getValue());
                comboFile.valueProperty().addListener((v, a, b) -> mod.getData().setProjectFile(comboFile.getValue()));
            }
            if (instance.hasMod(mod.getName())) {
                buttonInstall.setDisable(true);
            }
            buttonInstall.setVisible(true);
            mod.setProcess(mod.getName());
            DownloadTask task = DownloadManager.createDownload(mod.getName(), () -> mod.install(instance));
            buttonInstall.setOnAction(event -> {
                task.start();
                buttonInstall.setDisable(true);
            });
            task.setOnSucceeded(event -> {
                instance.verifyMods();
                if (!instance.hasMod(mod.getName()))
                    buttonInstall.setDisable(false);
            });
        }


    }
}
