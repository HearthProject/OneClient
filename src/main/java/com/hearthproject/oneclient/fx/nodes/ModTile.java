package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModInstaller;

public abstract class ModTile extends ProjectTile {


    protected ModInstaller mod;

    public ModTile(Instance instance, ModInstaller mod) {
        super(instance, mod);
        this.mod = mod;
    }


//    public ModTile(Instance instance, ModInstaller mod) {
//        this.instance = instance;
//        this.mod = mod;
//
//        FXMLLoader fxmlLoader = new FXMLLoader(FileUtil.getResource("gui/contentpanes/install_tile.fxml"));
//        fxmlLoader.setRoot(this);
//        fxmlLoader.setController(this);
//        try {
//            fxmlLoader.load();
//        } catch (IOException exception) {
//            throw new RuntimeException(exception);
//        }
//        MiscUtil.setupLink(title, mod.getName(), "");
//        imageView.setEffect(blurEffect);
//        blurEffect.radiusProperty().bind(nodePane.opacityProperty().multiply(10));
//        buttonInstall.setVisible(false);
//        nodePane.setOpacity(0F);
//        nodePane.hoverProperty().addListener((observable, oldValue, newValue) -> {
//            FadeTransition fadeTransition = new FadeTransition(new Duration(400), nodePane);
//            if (newValue) {
//                fadeTransition.setFromValue(0F);
//                fadeTransition.setToValue(1F);
//                fadeTransition.play();
//                nodePane.setOpacity(1F);
//            } else {
//                fadeTransition.setFromValue(1F);
//                fadeTransition.setToValue(0F);
//                fadeTransition.play();
//                nodePane.setOpacity(0F);
//            }
//        });
//        imageView.disableProperty().bind(instance.installingProperty());
//    }

}
