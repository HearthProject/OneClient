package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.fx.controllers.MinecraftAuthController;
import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.launcher.NotifyUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;

public class CurseModpack extends CurseTile {

	public CurseModpack(CurseElement pack) {
		super(pack);
	}

	@Override
	public void install() {
		if(!MinecraftAuthController.isUserValid()){
			MinecraftAuthController.updateGui();
			OneClientLogging.logUserError(new RuntimeException("You must log into minecraft to install the game!"), "You are not logged in!");
			return;
		}
		NotifyUtil.setText("Installing %s", element.getTitle());

		//		new Thread(() -> {
		//			Instance instance = null;
		//			try {
		////				instance = CursePackInstaller.downloadFromURL(element.getUrl(), "latest");
		//				File image = element.getIcon();
		//				if (image != null)
		//					Files.copy(image.toPath(), instance.getManifest().getIcon().toPath());
		//				MinecraftUtil.installMinecraft(instance);
		//			} catch (Throwable throwable) {
		//				OneClientLogging.error(throwable);
		//			}
		//			final Instance i = instance;
		//			Platform.runLater(() -> {
		//				InstanceManager.addInstance(i);
		//				if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
		//					Main.mainController.currentContent.refresh();
		//				}
		//				NotifyUtil.setText(Duration.seconds(10), "%s has been downloaded and installed!", i.getManifest().getName());
		//			});
		//		}).start();
	}
}
