package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModpackInstaller;

public class ModpackTile extends ProjectTile {


    public ModpackTile(Instance instance, ModpackInstaller installer) {
        super(instance, installer);
    }


//
//	public ModpackTile(Instance instance) {
//		this.instance = instance;
//
//		FXMLLoader fxmlLoader = new FXMLLoader(FileUtil.getResource("gui/contentpanes/install_tile.fxml"));
//		fxmlLoader.setRoot(this);
//		fxmlLoader.setController(this);
//		try {
//			fxmlLoader.load();
//		} catch (IOException exception) {
//			throw new RuntimeException(exception);
//		}
//
//		if (instance.getInstaller() instanceof CurseInstaller) {
//			CurseInstaller installer = (CurseInstaller) instance.getInstaller();
//			MiscUtil.setupLink(title, instance.getName(), Curse.getCurseForge(installer.projectId));
//			comboFile.setVisible(true);
//			comboFile.setItems(FXCollections.observableArrayList(installer.getProjectFiles()).sorted());
//			comboFile.getSelectionModel().selectFirst();
//			installer.setFile(comboFile.getValue());
//			comboFile.valueProperty().addListener((v, a, b) -> installer.setFile(b));
//		}
////		Label downloads = info("Downloads: %s", MiscUtil.formatNumbers((int) instance.tempInfo.get("downloads")));
////		Label gameVersions = info("Versions: %s", instance.tempInfo.get("gameVersions"));
////
////		right.getChildren().addAll(gameVersions, downloads);
////		left.getChildren().addAll(
////			info("By %s", instance.tempInfo.get("authors")),
////			info("%s", instance.tempInfo.get("summary"))
////		);
//
//		DownloadTask task = DownloadManager.createDownload(instance.getName(), instance::install);
//		buttonInstall.setOnAction(event -> {
//			task.start();
//			buttonInstall.setDisable(true);
//		});
//		task.setOnSucceeded(event -> buttonInstall.setDisable(false));
//		nodePane.setOpacity(0F);
//		nodePane.hoverProperty().addListener((observable, oldValue, newValue) -> {
//			FadeTransition fadeTransition = new FadeTransition(new Duration(400), nodePane);
//			if (newValue) {
//				fadeTransition.setFromValue(0F);
//				fadeTransition.setToValue(1F);
//				fadeTransition.play();
//				nodePane.setOpacity(1F);
//			} else {
//				fadeTransition.setFromValue(1F);
//				fadeTransition.setToValue(0F);
//				fadeTransition.play();
//				nodePane.setOpacity(0F);
//			}
//		});
//		imageView.disableProperty().bind(instance.installingProperty());
//	}
//
//
//
//	private String getName() {
//		return instance.name.trim();
//	}
//
//	@Override
//	public int compareTo(ModpackTile o) {
//		return getName().compareTo(o.getName());
//	}
}
