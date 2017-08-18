package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.ContentPanes;
import com.hearthproject.oneclient.json.models.curse.CursePacks;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.curse.CursePackInstaller;
import com.hearthproject.oneclient.util.curse.CursePackUtil;
import com.hearthproject.oneclient.util.launcher.InstanceManager;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class PackCardController {
    public ImageView modpackImage;
    public Text modpackName;
    public Text modpackDetails;
    public Text modpackDescription;

    public CursePacks.CursePack pack;

    public void buttonInstallPack(ActionEvent actionEvent) {
	    new Thread(() -> {
		    Instance instance = new Instance(pack.name);
		    try {
			    new CursePackInstaller().downloadFromURL(pack.webSiteURL, "latest", instance);
			    MinecraftUtil.installMinecraft(instance);
		    } catch (Throwable throwable) {
			    OneClientLogging.log(throwable);
		    }
		    InstanceManager.addInstance(instance);
		    if (Main.mainController.currentContent == ContentPanes.INSTANCES_PANE) {
			    Main.mainController.currentContent.refresh();
		    }
		    //TODO show instances
	    }).start();
    }
}
