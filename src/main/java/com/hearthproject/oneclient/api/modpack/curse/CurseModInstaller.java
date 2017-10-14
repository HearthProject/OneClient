package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModInstaller;
import com.hearthproject.oneclient.api.base.PackType;
import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.fx.nodes.CurseModTile;
import com.hearthproject.oneclient.fx.nodes.ModTile;
import javafx.scene.control.Alert;

import java.util.List;

public class CurseModInstaller extends ModInstaller {
    public transient boolean resolveDependencies;

    public CurseModInstaller(int projectID, int fileID) {
        super(PackType.CURSE, new CurseFileData(projectID, fileID));
    }

    @Override
    public ModTile createTile(Instance instance) {
        return new CurseModTile(instance, this);
    }

    @Override
    public void install(Instance instance) {
//        if (fileData == null) {
//            OneClientLogging.info("No File Selected");
//        }
//        try {
//            if (!fileData.required) {
////                DownloadManager.updateMessage(process, "%s - Skipping Disabled Mod %s", instance.getName(), FilenameUtils.getBaseName(fileData.getURL()));
//                return;
//            }
//            //TODO
////            if (resolveDependencies) {
////                for (CurseFullProject.CurseFile.Dependency dep : file.getDependencies()) {
////                    instance.verifyMods();
////                    if (dep.isRequired()) {
////                        CurseFullProject project = JsonUtil.read(Curse.getProjectURL(dep.AddOnId), CurseFullProject.class);
////                        CurseModInstaller depInstaller = new CurseModInstaller(instance, project);
////                        if (instance.hasMod(depInstaller.getName())) {
////                            OneClientLogging.info("{} - Dependency {} is already installed", instance.getName(), depInstaller.getName());
////                            continue;
////                        }
////                        depInstaller.setProcess(process);
////                        CurseFullProject.CurseFile file = depInstaller.getProjectFiles().stream().sorted(Comparator.comparing(CurseFullProject.CurseFile::getDate).reversed()).findFirst().orElse(null);
////                        depInstaller.setFile(file);
////                        DownloadManager.updateMessage(process, "%s - Installing Dependency %s for %s", instance.getName(), FilenameUtils.getBaseName(depInstaller.fileData.getURL()), FilenameUtils.getBaseName(fileData.getURL()));
////                        depInstaller.install(instance);
////                    }
////                }
////            }
////            if (!instance.hasMod(getName())) {
////                DownloadManager.updateMessage(process, "%s - Installing %s", instance.getName(), FilenameUtils.getBaseName(fileData.getURL()));
////                File jar = FileUtil.downloadToName(fileData.getURL(), instance.getModDirectory());
////                this.hash = new FileHash(jar);
////                instance.getMods().add(this);
////            }
//        } catch (Throwable e) {
//            OneClientLogging.error(e);
//        }
    }

    public void update(Instance instance, Database.ProjectFile file) {
//        if (file != null) {
//            getHash().getFile().delete();
//            setFile(file);
//            install(instance);
//            instance.verifyMods();
//            NotifyUtil.clear();
//        }
    }

    @Override
    public void update(Instance instance) {
//        CurseFullProject.CurseFile update = selectUpdate(instance.getGameVersion(), true);
//        update(instance, update);
    }

    @Override
    public CurseFileData getData() {
        return (CurseFileData) super.getData();
    }

    @Override
    public String getVersion() {
        return getData().getProject().getVersions().toString();
    }

    public boolean alert(List list) {
        if (list.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Updates Available");
            alert.setHeaderText("No Updates Available");
            alert.setGraphic(null);
            alert.showAndWait();
            return true;
        }
        return false;
    }

//    public CurseFullProject.CurseFile findLatestUpdate(String gameVersion, boolean onlyNew) {
//        NotifyUtil.setText("%s Checking for updates", getName());
//        List<CurseFullProject.CurseFile> updates = onlyNew ? Curse.getNewFiles(project.getId(), gameVersion, this.file) : Curse.getProjectFiles(project.getId(), gameVersion);
//        return updates.stream().sorted().findFirst().orElse(null);
//    }
//
//    public CurseFullProject.CurseFile selectUpdate(String gameVersion, boolean onlyNew) {
//        NotifyUtil.setText("%s Checking for updates", getName());
//        this.files = Curse.getProjectFiles(project.getId(), gameVersion);
//        if (this.files != null) {
//            List<CurseFullProject.CurseFile> updates = onlyNew ? Curse.getNewFiles(project.getId(), gameVersion, this.file) : Curse.getProjectFiles(project.getId(), gameVersion);
//            if (alert(updates))
//                return null;
//            CurseFullProject.CurseFile file = new UpdateDialog(updates).showAndWait().orElse(null);
//            this.file = file;
//            return file;
//        }
//        return null;
//    }

    public void shouldResolveDependencies(boolean resolveDependencies) {
        this.resolveDependencies = resolveDependencies;
    }

//    @Override
//    public CurseModTile createTile(Instance instance, boolean download) {
//        if (download)
//            return new CurseModTile.Download(instance, this);
//        return new CurseModTile.View(instance, this);
//    }


}


