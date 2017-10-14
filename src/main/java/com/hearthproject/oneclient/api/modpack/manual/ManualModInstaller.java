package com.hearthproject.oneclient.api.modpack.manual;

import com.hearthproject.oneclient.api.base.FileData;
import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.ModInstaller;
import com.hearthproject.oneclient.api.base.PackType;
import com.hearthproject.oneclient.fx.nodes.ManualModTile;
import com.hearthproject.oneclient.fx.nodes.ModTile;

public class ManualModInstaller extends ModInstaller {

    public ManualModInstaller(FileData data) {
        super(PackType.MANUAL, data);
    }

    @Override
    public ModTile createTile(Instance instance) {
        return new ManualModTile(instance, this);
    }

    @Override
    public void install(Instance instance) {
        //NO-OP
    }

    @Override
    public void update(Instance instance) {

    }

    @Override
    public String getVersion() {
        return "";
    }
}
