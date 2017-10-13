package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.DownloadTask;
import com.hearthproject.oneclient.api.modpack.DownloadManager;
import com.hearthproject.oneclient.api.modpack.Instance;
import com.hearthproject.oneclient.api.modpack.curse.Curse;
import com.hearthproject.oneclient.api.modpack.curse.CurseModInstaller;
import com.hearthproject.oneclient.util.MiscUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.apache.commons.io.FileUtils;

public class CurseModTile extends ModTile {
    public CurseModTile(Instance instance, CurseModInstaller mod) {
        super(instance, mod);
//        if(mod.project == null && mod.getFileData() != null) {
//            mod.project = JsonUtil.read(Curse.getProjectURL(mod.getFileData().projectID), Database.ProjectFile.class);
//        }
//        ImageUtil.ImageService service = new ImageUtil.ImageService(mod.project.getIcon(), mod.project.getId());
//        service.setOnSucceeded(event -> MiscUtil.runLaterIfNeeded(() -> imageView.setImage(service.getValue())));
//        service.start();
        MiscUtil.setupLink(title, mod.getName(), Curse.getCurseForge(mod.project.getId()));
        Label downloads = info("Downloads: %s", MiscUtil.formatNumbers(mod.project.getDownloads()));
        Label gameVersions = info("Versions: %s", mod.project.getVersions());
        Label authors = info("By %s", mod.project.getAuthors());
        right.setAlignment(Pos.BASELINE_RIGHT);
        right.getChildren().addAll(gameVersions, downloads);
        left.getChildren().addAll(authors);
    }

    public Label info(String format, Object... params) {
        return info(String.format(format, params));
    }

    public Label info(String value) {
        Label l = new Label(value);
        l.setId("oc-info-label");
        return l;
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

            Button test = new Button("Delete");
            test.setOnAction(event -> {
                new Thread(() -> {
                    FileUtils.deleteQuietly(mod.getHash().getFile());
                    MiscUtil.runLaterIfNeeded(() -> instance.mods.remove(mod));
                }).start();
            });
            left.getChildren().addAll(test);

        }
    }

    public static class Download extends CurseModTile {

        public Download(Instance instance, CurseModInstaller mod) {
            super(instance, mod);

            if (mod.getFiles() != null) {
                comboFile.setVisible(true);
                comboFile.setItems(FXCollections.observableArrayList(mod.getFiles()).sorted());
                comboFile.getSelectionModel().selectFirst();
                if (!comboFile.getItems().isEmpty()) {
                    mod.setFile(comboFile.getValue());
                    comboFile.valueProperty().addListener((v, a, b) -> mod.setFile(comboFile.getValue()));
                }
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
